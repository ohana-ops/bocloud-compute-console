package com.ruoyi.bocompute.schedule;

import java.util.Date;

/**
 * 调度请求参数
 *
 * @author bocloud
 */
public class ScheduleRequest
{
    /** 申请单ID */
    private Long applyId;

    /** 申请单编号 */
    private String applyNo;

    /** 资源池ID */
    private Long resourceId;

    /** 资源类型（gpu/npu/node/cpu） */
    private String resourceType;

    /** 需要的设备数量（cpu 类型表示核数） */
    private Integer count;

    /** 使用人ID */
    private Long userId;

    /** 使用人账号 */
    private String userName;

    /** 使用开始时间 */
    private Date beginTime;

    /** 使用结束时间 */
    private Date endTime;

    /** 分配单ID，先插入分配单后再传入 */
    private Long allocId;

    /**
     * 获取申请单ID
     *
     * @return 申请单ID
     */
    public Long getApplyId()
    {
        return applyId;
    }

    /**
     * 设置申请单ID
     *
     * @param applyId 申请单ID
     */
    public void setApplyId(Long applyId)
    {
        this.applyId = applyId;
    }

    /**
     * 获取申请单编号
     *
     * @return 申请单编号
     */
    public String getApplyNo()
    {
        return applyNo;
    }

    /**
     * 设置申请单编号
     *
     * @param applyNo 申请单编号
     */
    public void setApplyNo(String applyNo)
    {
        this.applyNo = applyNo;
    }

    /**
     * 获取资源池ID
     *
     * @return 资源池ID
     */
    public Long getResourceId()
    {
        return resourceId;
    }

    /**
     * 设置资源池ID
     *
     * @param resourceId 资源池ID
     */
    public void setResourceId(Long resourceId)
    {
        this.resourceId = resourceId;
    }

    /**
     * 获取资源类型
     *
     * @return 资源类型
     */
    public String getResourceType()
    {
        return resourceType;
    }

    /**
     * 设置资源类型
     *
     * @param resourceType 资源类型
     */
    public void setResourceType(String resourceType)
    {
        this.resourceType = resourceType;
    }

    /**
     * 获取需要的设备数量
     *
     * @return 需要的设备数量
     */
    public Integer getCount()
    {
        return count;
    }

    /**
     * 设置需要的设备数量
     *
     * @param count 需要的设备数量
     */
    public void setCount(Integer count)
    {
        this.count = count;
    }

    /**
     * 获取使用人ID
     *
     * @return 使用人ID
     */
    public Long getUserId()
    {
        return userId;
    }

    /**
     * 设置使用人ID
     *
     * @param userId 使用人ID
     */
    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    /**
     * 获取使用人账号
     *
     * @return 使用人账号
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * 设置使用人账号
     *
     * @param userName 使用人账号
     */
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    /**
     * 获取使用开始时间
     *
     * @return 使用开始时间
     */
    public Date getBeginTime()
    {
        return beginTime;
    }

    /**
     * 设置使用开始时间
     *
     * @param beginTime 使用开始时间
     */
    public void setBeginTime(Date beginTime)
    {
        this.beginTime = beginTime;
    }

    /**
     * 获取使用结束时间
     *
     * @return 使用结束时间
     */
    public Date getEndTime()
    {
        return endTime;
    }

    /**
     * 设置使用结束时间
     *
     * @param endTime 使用结束时间
     */
    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    /**
     * 获取分配单ID
     *
     * @return 分配单ID
     */
    public Long getAllocId()
    {
        return allocId;
    }

    /**
     * 设置分配单ID
     *
     * @param allocId 分配单ID
     */
    public void setAllocId(Long allocId)
    {
        this.allocId = allocId;
    }
}
