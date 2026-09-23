package com.ruoyi.bocompute.schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.ruoyi.bocompute.domain.BizDevice;
import com.ruoyi.bocompute.mapper.BizDeviceMapper;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;

/**
 * 设备池调度器（默认实现）
 * 
 * 选卡策略：同节点优先（binpack），即优先选一个能放下全部设备的节点，并在多个候选节点中
 * 挑空闲数最少的那个，减少碎片；单节点放不下时按节点空闲数从多到少跨节点拼凑。
 * 
 * 通过 bocompute.scheduler.type=device-pool 装配（缺省也是该实现）。
 *
 * @author bocloud
 */
@Service
@ConditionalOnProperty(prefix = "bocompute.scheduler", name = "type", havingValue = "device-pool", matchIfMissing = true)
public class DevicePoolScheduler implements ComputeScheduler
{
    /** 调度实现标识 */
    public static final String SCHEDULE_TYPE = "device-pool";

    @Autowired
    private BizDeviceMapper bizDeviceMapper;

    /**
     * 为申请单分配设备
     * gpu/npu/node 必须选中 count 台空闲设备并锁定；cpu 直接返回空列表。
     *
     * @param request 调度请求
     * @return 调度结果
     */
    @Override
    public ScheduleResult allocate(ScheduleRequest request)
    {
        ScheduleResult result = new ScheduleResult();
        result.setScheduleType(SCHEDULE_TYPE);
        // cpu 按核数扣减，不产生设备绑定
        if (DeviceCodeGenerator.TYPE_CPU.equals(request.getResourceType()))
        {
            result.setDevices(new ArrayList<BizDevice>());
            result.setMessage("CPU资源按核数扣减，无设备绑定");
            result.setDeviceCodes("");
            result.setNodeNames("");
            return result;
        }
        // 1、查出该池全部空闲设备（SQL 已按节点、槽位升序）
        List<BizDevice> idleDevices = bizDeviceMapper.selectIdleDevicesByResourceId(request.getResourceId());
        int idle = idleDevices == null ? 0 : idleDevices.size();
        // 2、空闲量不足直接失败，由外层事务整单回滚
        if (idle < request.getCount())
        {
            throw new ServiceException("空闲设备不足，需要" + request.getCount() + "，当前空闲" + idle);
        }
        // 3、按节点分组，组内保持槽位升序
        Map<String, List<BizDevice>> nodeMap = groupByNode(idleDevices);
        // 4、同节点优先（binpack），放不下再跨节点拼
        List<BizDevice> selected = selectByBinpack(nodeMap, request.getCount(), request.getResourceType(), result);
        // 5、锁定设备，影响行数必须等于申请数量，否则说明有并发抢占
        List<Long> deviceIds = new ArrayList<Long>();
        for (BizDevice device : selected)
        {
            deviceIds.add(device.getDeviceId());
        }
        int rows = bizDeviceMapper.lockDevicesByIds(deviceIds, request.getAllocId(), request.getApplyNo(),
                request.getUserName());
        if (rows != request.getCount())
        {
            throw new ServiceException("设备锁定失败，可能被并发占用，请重试");
        }
        // 6、组装回写字段
        result.setDevices(selected);
        result.setDeviceCodes(joinDeviceCodes(selected));
        result.setNodeNames(joinNodeNames(selected));
        return result;
    }

    /**
     * 释放某次分配绑定的全部设备
     *
     * @param allocId 分配单ID
     */
    @Override
    public void release(Long allocId)
    {
        if (StringUtils.isNull(allocId))
        {
            return;
        }
        bizDeviceMapper.releaseDevicesByAllocId(allocId);
    }

    /**
     * 按节点名分组，保持节点首次出现顺序，组内保持槽位升序
     *
     * @param devices 设备列表
     * @return 节点 -> 该节点空闲设备列表
     */
    private Map<String, List<BizDevice>> groupByNode(List<BizDevice> devices)
    {
        Map<String, List<BizDevice>> nodeMap = new LinkedHashMap<String, List<BizDevice>>();
        if (StringUtils.isNull(devices))
        {
            return nodeMap;
        }
        for (BizDevice device : devices)
        {
            List<BizDevice> list = nodeMap.get(device.getNodeName());
            if (StringUtils.isNull(list))
            {
                list = new ArrayList<BizDevice>();
                nodeMap.put(device.getNodeName(), list);
            }
            list.add(device);
        }
        return nodeMap;
    }

    /**
     * 同节点优先（binpack）选卡
     * 1、优先找空闲数 >= count 的节点，多个候选取空闲数最少的（减少碎片）
     * 2、没有单节点能放下，则按空闲数从多到少跨节点拼凑
     *
     * @param nodeMap 节点分组
     * @param count 需要的设备数量
     * @param resourceType 资源类型，用于拼接说明文案
     * @param result 调度结果，用于回写说明文案
     * @return 选中的设备列表
     */
    private List<BizDevice> selectByBinpack(Map<String, List<BizDevice>> nodeMap, int count, String resourceType,
            ScheduleResult result)
    {
        // 1、找能单独放下的节点，取其中空闲数最少的一个
        String bestNode = null;
        for (Map.Entry<String, List<BizDevice>> entry : nodeMap.entrySet())
        {
            if (entry.getValue().size() >= count)
            {
                if (bestNode == null || entry.getValue().size() < nodeMap.get(bestNode).size())
                {
                    bestNode = entry.getKey();
                }
            }
        }
        if (bestNode != null)
        {
            List<BizDevice> selected = new ArrayList<BizDevice>(nodeMap.get(bestNode).subList(0, count));
            result.setMessage("同节点 " + bestNode + " 分配 " + count + " " + unit(resourceType));
            return selected;
        }
        // 2、跨节点拼凑：按节点空闲数从多到少，依次取用直到凑满
        List<Map.Entry<String, List<BizDevice>>> entries = new ArrayList<Map.Entry<String, List<BizDevice>>>(
                nodeMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, List<BizDevice>>>()
        {
            @Override
            public int compare(Map.Entry<String, List<BizDevice>> o1, Map.Entry<String, List<BizDevice>> o2)
            {
                // 空闲数多的排在前面
                return o2.getValue().size() - o1.getValue().size();
            }
        });
        List<BizDevice> selected = new ArrayList<BizDevice>();
        for (Map.Entry<String, List<BizDevice>> entry : entries)
        {
            for (BizDevice device : entry.getValue())
            {
                if (selected.size() >= count)
                {
                    break;
                }
                selected.add(device);
            }
            if (selected.size() >= count)
            {
                break;
            }
        }
        result.setMessage("跨节点分配: " + joinNodeNames(selected));
        return selected;
    }

    /**
     * 拼接设备编号，逗号分隔
     *
     * @param devices 设备列表
     * @return 设备编号拼接串
     */
    private String joinDeviceCodes(List<BizDevice> devices)
    {
        StringBuilder sb = new StringBuilder();
        for (BizDevice device : devices)
        {
            if (sb.length() > 0)
            {
                sb.append(",");
            }
            sb.append(device.getDeviceCode());
        }
        return sb.toString();
    }

    /**
     * 拼接去重的节点名，逗号分隔
     *
     * @param devices 设备列表
     * @return 节点名拼接串
     */
    private String joinNodeNames(List<BizDevice> devices)
    {
        StringBuilder sb = new StringBuilder();
        List<String> nodes = new ArrayList<String>();
        for (BizDevice device : devices)
        {
            if (!nodes.contains(device.getNodeName()))
            {
                nodes.add(device.getNodeName());
            }
        }
        for (String node : nodes)
        {
            if (sb.length() > 0)
            {
                sb.append(",");
            }
            sb.append(node);
        }
        return sb.toString();
    }

    /**
     * 根据资源类型返回计量单位
     *
     * @param resourceType 资源类型
     * @return 计量单位
     */
    private String unit(String resourceType)
    {
        if (DeviceCodeGenerator.TYPE_NODE.equals(resourceType))
        {
            return "台";
        }
        return "卡";
    }
}
