package com.ruoyi.bocompute.schedule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.ruoyi.bocompute.domain.BizDevice;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeCondition;
import io.fabric8.kubernetes.api.model.Pod;

/**
 * K8s 调度器（fabric8 真实接入实现）
 *
 * 用 fabric8 KubernetesClient 完成算力编排，调度链路：
 * 1、allocate：查集群节点 → 读各节点 status.allocatable 中的 nvidia.com/gpu（Device Plugin 上报）
 *    → 过滤可调度节点并 binpack 选节点（GPU 扩展资源不可跨节点拆分，必须单节点放下）
 *    → 组装 Pod 规格（resources.limits 声明 nvidia.com/gpu，nodeName 绑定选中节点）
 *    → 创建 Pod 并打上 label bocompute/alloc-id = 分配单ID
 *    → 回填 ScheduleResult（deviceCodes 记 Pod 名，nodeNames 记调度节点）；
 * 2、release：按 label bocompute/alloc-id 精确删除该分配单的 Pod，
 *    Pod 删除后 Device Plugin 随之回收 GPU，无需维护「Pod ↔ 分配单」映射表；
 * 3、调度结果仍封装成 ScheduleResult，上层 passApply / releaseAlloc 无需改动。
 *
 * 职责边界：GPU 的真实分配由 K8s scheduler + NVIDIA Device Plugin 完成，
 * 本类只做「选节点 + 提交 Pod 规格 + 记录结果」，不参与内核态调度。
 *
 * 启用条件：bocompute.scheduler.type=k8s，且需真实集群、GPU 节点已装 Device Plugin、
 * 并准备好 kubeconfig；CPU 类型按核数扣减，不创建 Pod。
 *
 * @author bocloud
 */
@Service
@ConditionalOnProperty(prefix = "bocompute.scheduler", name = "type", havingValue = "k8s")
public class K8sComputeScheduler implements ComputeScheduler
{
    /** 调度实现标识 */
    public static final String SCHEDULE_TYPE = "k8s";

    /** 日志对象 */
    private static final Logger log = LoggerFactory.getLogger(K8sComputeScheduler.class);

    /** K8s 客户端模板，fabric8 底层 API 全部收拢在该模板内 */
    @Autowired
    private K8sTemplate k8sTemplate;

    /**
     * 为申请单分配算力（真实 K8s 调度链路）
     * 链路：查节点 GPU → 过滤可调度节点 → binpack 选节点 → 组装 Pod 规格 → 创建 Pod → 回填结果。
     * 说明：GPU 的实际分配由 K8s scheduler + NVIDIA Device Plugin 完成，
     * 本方法只负责「选出满足条件的节点并提交 Pod 规格」，不参与内核态调度。
     *
     * @param request 调度请求
     * @return 调度结果，devices 为空列表（K8s 侧不落地 BizDevice 行）
     */
    @Override
    public ScheduleResult allocate(ScheduleRequest request)
    {
        ScheduleResult result = new ScheduleResult();
        result.setScheduleType(SCHEDULE_TYPE);
        // K8s 侧不产生 BizDevice 行，设备列表恒为空，上层据此不写绑定明细
        result.setDevices(new ArrayList<BizDevice>());
        // cpu 按核数扣减资源池，不创建 Pod
        if (DeviceCodeGenerator.TYPE_CPU.equals(request.getResourceType()))
        {
            result.setMessage("CPU资源按核数扣减，K8s 调度器不创建 Pod");
            result.setDeviceCodes("");
            result.setNodeNames("");
            return result;
        }
        int count = request.getCount() == null ? 0 : request.getCount();
        if (count <= 0)
        {
            throw new ServiceException("申请数量非法，必须大于 0");
        }
        if (StringUtils.isNull(request.getAllocId()))
        {
            throw new ServiceException("分配单ID为空，无法创建算力 Pod");
        }
        // 1、查询集群节点，并算出每个可调度节点上剩余可分配的 GPU 数
        Map<String, Integer> nodeGpuMap = collectSchedulableGpu(k8sTemplate.listNodes());
        int total = sumGpu(nodeGpuMap);
        // 2、总量不足直接失败，由外层事务整单回滚
        if (total < count)
        {
            throw new ServiceException("集群 GPU 资源不足，需要" + count + "，当前可分配" + total);
        }
        // 3、binpack 选节点：GPU 扩展资源不能跨节点拆给一个 Pod，必须单节点放下
        String targetNode = selectNode(nodeGpuMap, count);
        if (StringUtils.isEmpty(targetNode))
        {
            throw new ServiceException("集群无可容纳 " + count + " 卡的节点，GPU 资源不可跨节点拆分");
        }
        // 4、提交 Pod 规格：limits 声明 nvidia.com/gpu，并绑定上一步选出的节点
        Pod pod = k8sTemplate.createPod(request.getAllocId(), request.getApplyNo(), targetNode, count);
        // 5、回填调度结果：设备编号记 Pod 名，节点记实际调度节点
        String podName = pod.getMetadata() == null ? "" : pod.getMetadata().getName();
        result.setDeviceCodes(podName);
        result.setNodeNames(targetNode);
        result.setMessage("K8s 调度至节点 " + targetNode + "，创建 Pod " + podName + " 申请 " + count + " 卡 GPU"
                + "（由 NVIDIA Device Plugin 完成实际分配）");
        log.info("K8s 调度完成，allocId={}, applyNo={}, node={}, gpu={}, pod={}", request.getAllocId(),
                request.getApplyNo(), targetNode, count, podName);
        return result;
    }

    /**
     * 统计每个可调度节点上剩余可分配的 GPU 数量
     * 数据来源是各节点 status.allocatable 中的 nvidia.com/gpu（Device Plugin 上报）。
     *
     * @param nodes 集群节点列表
     * @return 节点名 -> 该节点可分配 GPU 数，保持节点原始顺序
     */
    private Map<String, Integer> collectSchedulableGpu(List<Node> nodes)
    {
        Map<String, Integer> nodeGpuMap = new LinkedHashMap<String, Integer>();
        if (StringUtils.isNull(nodes))
        {
            return nodeGpuMap;
        }
        for (Node node : nodes)
        {
            // 跳过 cordon、NotReady 等不可调度节点
            if (!isSchedulable(node))
            {
                continue;
            }
            // 跳过节点名为空的异常节点
            String nodeName = node.getMetadata() == null ? null : node.getMetadata().getName();
            if (StringUtils.isEmpty(nodeName))
            {
                continue;
            }
            int gpu = k8sTemplate.gpuAllocatable(node);
            // 无 GPU 的节点不参与算力调度
            if (gpu > 0)
            {
                nodeGpuMap.put(nodeName, gpu);
            }
        }
        return nodeGpuMap;
    }

    /**
     * 判断节点是否可调度
     * 两个条件同时满足才算可调度：未被 cordon（spec.unschedulable 不为 true），且 Ready 条件为 True。
     *
     * @param node 节点对象
     * @return 可调度返回 true
     */
    private boolean isSchedulable(Node node)
    {
        if (StringUtils.isNull(node))
        {
            return false;
        }
        // 被 cordon 的节点不允许再调度新 Pod
        if (node.getSpec() != null && Boolean.TRUE.equals(node.getSpec().getUnschedulable()))
        {
            return false;
        }
        if (StringUtils.isNull(node.getStatus()) || StringUtils.isNull(node.getStatus().getConditions()))
        {
            return false;
        }
        for (NodeCondition condition : node.getStatus().getConditions())
        {
            if (StringUtils.isNull(condition))
            {
                continue;
            }
            // Ready 为 True 才认为节点健康
            if ("Ready".equals(condition.getType()) && "True".equals(condition.getStatus()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 统计节点 GPU 总量
     *
     * @param nodeGpuMap 节点名 -> 可分配 GPU 数
     * @return 集群可分配 GPU 总数
     */
    private int sumGpu(Map<String, Integer> nodeGpuMap)
    {
        int total = 0;
        for (Map.Entry<String, Integer> entry : nodeGpuMap.entrySet())
        {
            total += entry.getValue() == null ? 0 : entry.getValue();
        }
        return total;
    }

    /**
     * binpack 选节点：在能单独放下 count 卡的节点中，挑剩余 GPU 最少的那个，减少碎片
     *
     * @param nodeGpuMap 节点名 -> 可分配 GPU 数
     * @param count 需要的 GPU 数量
     * @return 选中的节点名，无满足条件的节点返回 null
     */
    private String selectNode(Map<String, Integer> nodeGpuMap, int count)
    {
        String bestNode = null;
        int bestGpu = 0;
        for (Map.Entry<String, Integer> entry : nodeGpuMap.entrySet())
        {
            int gpu = entry.getValue() == null ? 0 : entry.getValue();
            if (gpu < count)
            {
                continue;
            }
            // 首次命中，或者剩余更少（碎片更小）则替换
            if (bestNode == null || gpu < bestGpu)
            {
                bestNode = entry.getKey();
                bestGpu = gpu;
            }
        }
        return bestNode;
    }

    /**
     * 释放某次分配绑定的算力
     * 与 allocate 的标签绑定一一对应：按 label bocompute/alloc-id 精确删除该分配单创建的 Pod，
     * Pod 删除后 K8s 回收容器，NVIDIA Device Plugin 随之释放占用的 GPU，无需维护映射表。
     *
     * @param allocId 分配单ID，为空直接返回
     */
    @Override
    public void release(Long allocId)
    {
        // 空值保护：没有分配单ID不做任何删除，避免按空标签误删整个命名空间的 Pod
        if (StringUtils.isNull(allocId))
        {
            return;
        }
        // 按 label 删除该分配单对应的全部 Pod，异常由模板转 ServiceException 抛出回滚
        k8sTemplate.deletePodByAllocId(allocId);
        log.info("K8s 释放算力完成，allocId={}, namespace={}", allocId, k8sTemplate.getNamespace());
    }
}
