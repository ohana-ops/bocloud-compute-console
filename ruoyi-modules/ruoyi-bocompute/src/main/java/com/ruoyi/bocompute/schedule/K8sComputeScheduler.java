package com.ruoyi.bocompute.schedule;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.exception.ServiceException;

/**
 * K8s 调度器（预留实现，未接入真实集群）
 * 
 * 替换方式说明：
 * 1、将配置项 bocompute.scheduler.type 改为 k8s，本实现会被装配；
 * 2、allocate 中改为调用 Kubernetes API：按 resourceType 组装 nodeSelector / resource.limits
 *    （如 nvidia.com/gpu: N），创建 Pod 并等待调度成功，把调度到的 nodeName 与设备 UUID 回填；
 * 3、release 中改为删除对应 Pod，并等待 Device Plugin 释放设备；
 * 4、调度结果仍封装成 ScheduleResult，上层 passApply / releaseAlloc 无需改动。
 * 
 * 当前所有方法均直接抛出「未接入」异常，审批通过时会整单回滚。
 *
 * @author bocloud
 */
@Service
@ConditionalOnProperty(prefix = "bocompute.scheduler", name = "type", havingValue = "k8s")
public class K8sComputeScheduler implements ComputeScheduler
{
    /** 调度实现标识 */
    public static final String SCHEDULE_TYPE = "k8s";

    /**
     * 为申请单分配设备（K8s 调度器未接入，直接失败）
     *
     * @param request 调度请求
     * @return 调度结果
     */
    @Override
    public ScheduleResult allocate(ScheduleRequest request)
    {
        throw new ServiceException("K8s 调度器未接入");
    }

    /**
     * 释放某次分配绑定的全部设备（K8s 调度器未接入，直接失败）
     *
     * @param allocId 分配单ID
     */
    @Override
    public void release(Long allocId)
    {
        throw new ServiceException("K8s 调度器未接入");
    }
}
