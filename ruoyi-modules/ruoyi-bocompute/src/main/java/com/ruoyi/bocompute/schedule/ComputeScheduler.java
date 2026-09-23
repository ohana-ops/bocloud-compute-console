package com.ruoyi.bocompute.schedule;

/**
 * 算力调度器接口
 * 
 * 只做「选设备、锁设备、还设备」三件事，库存回写与申请单状态由调用方在同一事务内处理。
 * 当前有设备池实现（DevicePoolScheduler）与 K8s 预留实现（K8sComputeScheduler）两个实现，
 * 通过配置项 bocompute.scheduler.type 切换。
 *
 * @author bocloud
 */
public interface ComputeScheduler
{
    /**
     * 为申请单分配设备。
     * gpu/npu/node：必须选中 applyCount 台空闲设备；
     * cpu：不选设备，只返回空设备列表，由调用方走数量扣减。
     * 选不够必须抛 ServiceException，由外层事务回滚。
     *
     * @param request 调度请求
     * @return 调度结果
     */
    ScheduleResult allocate(ScheduleRequest request);

    /**
     * 释放某次分配绑定的全部设备。
     *
     * @param allocId 分配单ID
     */
    void release(Long allocId);
}
