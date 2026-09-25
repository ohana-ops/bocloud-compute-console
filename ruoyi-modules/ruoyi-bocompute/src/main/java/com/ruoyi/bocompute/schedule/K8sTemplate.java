package com.ruoyi.bocompute.schedule;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import jakarta.annotation.PreDestroy;

/**
 * K8s 客户端模板（fabric8 KubernetesClient 封装）
 *
 * 职责：把 fabric8 的底层 API 全部收拢在本类内部，K8sComputeScheduler 只面对
 * 「查节点、读 GPU、建 Pod、删 Pod」四个业务语义方法，不直接接触 KubernetesClient。
 *
 * 客户端创建策略：读 kubeconfig（配置项 > 环境变量 KUBECONFIG > ~/.kube/config），
 * 懒加载单例 —— 只有第一次真正调用 K8s 时才建立连接，
 * 因此 bocompute.scheduler.type=device-pool 时本类被装配也不会连集群、不影响启动。
 *
 * 职责边界：GPU 的实际分配由 K8s scheduler + NVIDIA Device Plugin 完成，
 * 本类只负责「提交 Pod 规格」与「按 label 删除 Pod」，不参与内核态调度。
 *
 * @author bocloud
 */
@Component
public class K8sTemplate
{
    /** 日志对象 */
    private static final Logger log = LoggerFactory.getLogger(K8sTemplate.class);

    /** NVIDIA Device Plugin 上报的 GPU 扩展资源名 */
    public static final String GPU_RESOURCE_KEY = "nvidia.com/gpu";

    /** Pod label：分配单ID，释放时按此 label 精确删除，无需维护映射表 */
    public static final String LABEL_ALLOC_ID = "bocompute/alloc-id";

    /** Pod label：申请单编号，便于在集群侧按单据排查 */
    public static final String LABEL_APPLY_NO = "bocompute/apply-no";

    /** Pod 名称前缀，拼接分配单ID后必须满足 RFC1123 命名规范 */
    private static final String POD_NAME_PREFIX = "bocompute-alloc-";

    /** 算力 Pod 的容器名 */
    private static final String CONTAINER_NAME = "bocompute-workload";

    /** 默认 kubeconfig 路径：~/.kube/config */
    private static final String DEFAULT_KUBECONFIG = System.getProperty("user.home") + "/.kube/config";

    /** kubeconfig 路径环境变量名 */
    private static final String ENV_KUBECONFIG = "KUBECONFIG";

    /** kubeconfig 路径，留空则取环境变量 KUBECONFIG，再兜底 ~/.kube/config */
    @Value("${bocompute.scheduler.k8s.kubeconfig:}")
    private String kubeConfigPath;

    /** 算力 Pod 所在的命名空间 */
    @Value("${bocompute.scheduler.k8s.namespace:default}")
    private String namespace;

    /** 算力 Pod 的容器镜像，仅作占位，真实业务镜像可覆盖该配置 */
    @Value("${bocompute.scheduler.k8s.image:nvidia/cuda:12.2.0-base-ubuntu22.04}")
    private String podImage;

    /** fabric8 客户端，懒加载单例，由 getClient() 统一创建 */
    private volatile KubernetesClient client;

    /**
     * 获取 KubernetesClient（懒加载单例）
     * 首次调用时才读 kubeconfig 并建立客户端，后续复用同一实例。
     *
     * @return KubernetesClient 实例
     */
    public synchronized KubernetesClient getClient()
    {
        // 双重检查，避免并发调用重复创建客户端
        if (StringUtils.isNull(client))
        {
            try
            {
                client = buildClient();
            }
            catch (IOException e)
            {
                log.error("K8s 客户端初始化失败，kubeconfig={}", resolveKubeConfigPath(), e);
                throw new ServiceException("K8s 客户端初始化失败：" + e.getMessage());
            }
        }
        return client;
    }

    /**
     * 查询集群节点列表
     * 返回的是节点快照，调用方据此读取各节点的 GPU 可分配量。
     *
     * @return 节点列表，查询不到时返回空列表
     */
    public List<Node> listNodes()
    {
        try
        {
            List<Node> nodes = getClient().nodes().list().getItems();
            // fabric8 在极端情况下可能返回 null，这里统一兜底成空列表
            return StringUtils.isNull(nodes) ? new ArrayList<Node>() : nodes;
        }
        catch (Exception e)
        {
            log.error("查询 K8s 节点列表失败", e);
            throw new ServiceException("查询 K8s 节点列表失败：" + e.getMessage());
        }
    }

    /**
     * 读取节点可分配的 GPU 数量
     * 数据来源是节点 status.allocatable 中的 nvidia.com/gpu，
     * 由 NVIDIA Device Plugin 上报，等价于该节点当前未被占用的卡数。
     *
     * @param node 节点对象
     * @return 可分配 GPU 数量，无 GPU 或读取失败返回 0
     */
    public int gpuAllocatable(Node node)
    {
        // 节点或状态为空，视为无 GPU
        if (StringUtils.isNull(node) || StringUtils.isNull(node.getStatus()))
        {
            return 0;
        }
        Map<String, Quantity> allocatable = node.getStatus().getAllocatable();
        if (StringUtils.isNull(allocatable))
        {
            return 0;
        }
        return parseQuantity(allocatable.get(GPU_RESOURCE_KEY));
    }

    /**
     * 按申请规格组装并创建算力 Pod
     * 关键点：
     * 1、容器 resources.limits / requests 声明 nvidia.com/gpu = gpuCount，交给 Device Plugin 分配；
     * 2、nodeName 指定到预选出的节点，避免 K8s 调度器把 Pod 放到别的节点导致结果与记录不符；
     * 3、打上 bocompute/alloc-id 标签，释放时按标签精确删除，无需维护「Pod ↔ 分配单」映射表。
     *
     * @param allocId 分配单ID，用于 Pod 命名与标签绑定
     * @param applyNo 申请单编号，写入标签便于排查
     * @param nodeName 预选出的目标节点名
     * @param gpuCount 申请的 GPU 数量
     * @return 创建成功的 Pod 对象
     */
    public Pod createPod(Long allocId, String applyNo, String nodeName, int gpuCount)
    {
        String podName = buildPodName(allocId);
        Pod pod = new PodBuilder()
                .withNewMetadata()
                    .withName(podName)
                    .withNamespace(namespace)
                    // 分配单ID标签：释放时的唯一依据
                    .addToLabels(LABEL_ALLOC_ID, String.valueOf(allocId))
                    // 申请单编号标签：集群侧排查用，允许为空值
                    .addToLabels(LABEL_APPLY_NO, StringUtils.isBlank(applyNo) ? "" : applyNo)
                .endMetadata()
                .withNewSpec()
                    // 占位负载不重启，避免释放前反复拉起
                    .withRestartPolicy("Never")
                    // 直接绑定预选节点，保证与回填的 nodeNames 一致
                    .withNodeName(nodeName)
                    .addNewContainer()
                        .withName(CONTAINER_NAME)
                        .withImage(podImage)
                        .withCommand("sleep", "infinity")
                        .withNewResources()
                            // GPU 是扩展资源，limits 与 requests 必须相等且为整数
                            .addToLimits(GPU_RESOURCE_KEY, new Quantity(String.valueOf(gpuCount)))
                            .addToRequests(GPU_RESOURCE_KEY, new Quantity(String.valueOf(gpuCount)))
                        .endResources()
                    .endContainer()
                .endSpec()
                .build();
        try
        {
            Pod created = getClient().pods().inNamespace(namespace).resource(pod).create();
            log.info("K8s 创建算力 Pod 成功，pod={}, node={}, gpu={}, allocId={}", podName, nodeName, gpuCount,
                    allocId);
            return created;
        }
        catch (Exception e)
        {
            log.error("K8s 创建算力 Pod 失败，pod={}, node={}, gpu={}, allocId={}", podName, nodeName, gpuCount,
                    allocId, e);
            throw new ServiceException("K8s 创建算力 Pod 失败：" + e.getMessage());
        }
    }

    /**
     * 按 label bocompute/alloc-id 删除该分配单对应的全部 Pod
     * 与 createPod 的标签绑定一一对应，删 Pod 即等价于释放 GPU（Device Plugin 随之回收设备）。
     *
     * @param allocId 分配单ID，为空直接返回
     */
    public void deletePodByAllocId(Long allocId)
    {
        // 空值保护：没有分配单ID就不做任何删除，避免误删整命名空间
        if (StringUtils.isNull(allocId))
        {
            return;
        }
        try
        {
            List<Pod> pods = listPodsByAllocId(allocId);
            if (pods.isEmpty())
            {
                log.info("K8s 未找到待释放的算力 Pod，allocId={}", allocId);
                return;
            }
            List<StatusDetails> details = getClient().pods().inNamespace(namespace)
                    .withLabel(LABEL_ALLOC_ID, String.valueOf(allocId)).delete();
            int deleted = StringUtils.isNull(details) ? pods.size() : details.size();
            log.info("K8s 删除算力 Pod 完成，allocId={}, 删除数量={}", allocId, deleted);
        }
        catch (Exception e)
        {
            log.error("K8s 删除算力 Pod 失败，allocId={}", allocId, e);
            throw new ServiceException("K8s 删除算力 Pod 失败：" + e.getMessage());
        }
    }

    /**
     * 按 label bocompute/alloc-id 查询该分配单对应的 Pod 列表
     *
     * @param allocId 分配单ID
     * @return Pod 列表，为空时返回空列表
     */
    public List<Pod> listPodsByAllocId(Long allocId)
    {
        if (StringUtils.isNull(allocId))
        {
            return new ArrayList<Pod>();
        }
        List<Pod> pods = getClient().pods().inNamespace(namespace)
                .withLabel(LABEL_ALLOC_ID, String.valueOf(allocId)).list().getItems();
        return StringUtils.isNull(pods) ? new ArrayList<Pod>() : pods;
    }

    /**
     * 关闭 KubernetesClient，容器销毁时释放连接资源
     */
    @PreDestroy
    public void close()
    {
        KubernetesClient current = client;
        if (StringUtils.isNotNull(current))
        {
            try
            {
                current.close();
                log.info("K8s 客户端已关闭");
            }
            catch (Exception e)
            {
                log.warn("K8s 客户端关闭异常", e);
            }
            client = null;
        }
    }

    /**
     * 获取当前使用的命名空间
     *
     * @return 命名空间
     */
    public String getNamespace()
    {
        return namespace;
    }

    /**
     * 构建客户端实例：读 kubeconfig 交给 fabric8 组装
     *
     * @return KubernetesClient 实例
     * @throws IOException kubeconfig 读取失败时抛出
     */
    private KubernetesClient buildClient() throws IOException
    {
        String path = resolveKubeConfigPath();
        Config config = Config.fromKubeconfig(readKubeConfig(path));
        log.info("K8s 客户端初始化完成，kubeconfig={}, master={}, namespace={}", path, config.getMasterUrl(),
                namespace);
        return new KubernetesClientBuilder().withConfig(config).build();
    }

    /**
     * 读取 kubeconfig 文件全文
     *
     * @param path kubeconfig 路径
     * @return kubeconfig 文本内容
     * @throws IOException 文件不存在或读取失败时抛出
     */
    private String readKubeConfig(String path) throws IOException
    {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    /**
     * 解析 kubeconfig 路径：配置项 > 环境变量 KUBECONFIG > ~/.kube/config
     *
     * @return kubeconfig 路径
     */
    private String resolveKubeConfigPath()
    {
        // 1、显式配置优先
        if (StringUtils.isNotEmpty(kubeConfigPath))
        {
            return kubeConfigPath.trim();
        }
        // 2、其次环境变量 KUBECONFIG
        String envPath = System.getenv(ENV_KUBECONFIG);
        if (StringUtils.isNotEmpty(envPath))
        {
            return envPath.trim();
        }
        // 3、兜底当前用户目录下的 ~/.kube/config
        return DEFAULT_KUBECONFIG;
    }

    /**
     * 解析 Quantity 为整数
     * GPU 扩展资源以字符串形式上报（如 "8"），这里按数值解析，
     * 兼容 "8" 与 "8.0" 两种写法，解析失败按 0 处理。
     *
     * @param quantity 资源量对象
     * @return 整数值，解析失败返回 0
     */
    private int parseQuantity(Quantity quantity)
    {
        if (StringUtils.isNull(quantity) || StringUtils.isEmpty(quantity.getAmount()))
        {
            return 0;
        }
        try
        {
            return new BigDecimal(quantity.getAmount().trim()).intValue();
        }
        catch (NumberFormatException e)
        {
            log.warn("GPU 数量解析失败，amount={}", quantity.getAmount());
            return 0;
        }
    }

    /**
     * 生成 Pod 名称：bocompute-alloc-{allocId}
     * 只用分配单ID，天然满足小写字母、数字、短横线的 RFC1123 命名规范。
     *
     * @param allocId 分配单ID
     * @return Pod 名称
     */
    private String buildPodName(Long allocId)
    {
        return POD_NAME_PREFIX + allocId;
    }
}
