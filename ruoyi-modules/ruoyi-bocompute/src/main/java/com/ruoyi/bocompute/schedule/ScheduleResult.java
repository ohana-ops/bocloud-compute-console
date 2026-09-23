package com.ruoyi.bocompute.schedule;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.bocompute.domain.BizDevice;

/**
 * 调度结果
 *
 * @author bocloud
 */
public class ScheduleResult
{
    /** 分配到的设备列表，cpu 类型为空列表 */
    private List<BizDevice> devices = new ArrayList<BizDevice>();

    /** 调度实现标识，如 device-pool */
    private String scheduleType;

    /** 调度结果说明，如：同节点 gpu-nj-01 分配 8 卡 */
    private String message;

    /** 设备编号逗号拼接 */
    private String deviceCodes;

    /** 涉及节点去重后逗号拼接 */
    private String nodeNames;

    /**
     * 获取分配到的设备列表
     *
     * @return 分配到的设备列表
     */
    public List<BizDevice> getDevices()
    {
        return devices;
    }

    /**
     * 设置分配到的设备列表
     *
     * @param devices 分配到的设备列表
     */
    public void setDevices(List<BizDevice> devices)
    {
        this.devices = devices;
    }

    /**
     * 获取调度实现标识
     *
     * @return 调度实现标识
     */
    public String getScheduleType()
    {
        return scheduleType;
    }

    /**
     * 设置调度实现标识
     *
     * @param scheduleType 调度实现标识
     */
    public void setScheduleType(String scheduleType)
    {
        this.scheduleType = scheduleType;
    }

    /**
     * 获取调度结果说明
     *
     * @return 调度结果说明
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * 设置调度结果说明
     *
     * @param message 调度结果说明
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * 获取设备编号逗号拼接
     *
     * @return 设备编号逗号拼接
     */
    public String getDeviceCodes()
    {
        return deviceCodes;
    }

    /**
     * 设置设备编号逗号拼接
     *
     * @param deviceCodes 设备编号逗号拼接
     */
    public void setDeviceCodes(String deviceCodes)
    {
        this.deviceCodes = deviceCodes;
    }

    /**
     * 获取涉及节点去重后逗号拼接
     *
     * @return 涉及节点去重后逗号拼接
     */
    public String getNodeNames()
    {
        return nodeNames;
    }

    /**
     * 设置涉及节点去重后逗号拼接
     *
     * @param nodeNames 涉及节点去重后逗号拼接
     */
    public void setNodeNames(String nodeNames)
    {
        this.nodeNames = nodeNames;
    }
}
