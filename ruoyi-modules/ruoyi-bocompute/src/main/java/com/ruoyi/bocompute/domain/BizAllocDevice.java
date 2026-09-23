package com.ruoyi.bocompute.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.annotation.Excel.ColumnType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 分配单-设备绑定对象 biz_alloc_device
 *
 * @author bocloud
 */
@TableName("biz_alloc_device")
public class BizAllocDevice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 绑定状态：占用中 */
    public static final String STATUS_USING = "0";

    /** 绑定状态：已释放 */
    public static final String STATUS_RELEASED = "1";

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    @Excel(name = "主键ID", cellType = ColumnType.NUMERIC)
    private Long id;

    /** 分配单ID */
    private Long allocId;

    /** 申请单ID */
    private Long applyId;

    /** 设备ID */
    private Long deviceId;

    /** 设备编号（冗余） */
    @Excel(name = "设备编号")
    private String deviceCode;

    /** 宿主机节点名（冗余） */
    @Excel(name = "节点")
    private String nodeName;

    /** 资源池ID */
    private Long resourceId;

    /** 绑定时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "绑定时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date bindTime;

    /** 释放时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "释放时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date releaseTime;

    /** 绑定状态（0占用中 1已释放） */
    @Excel(name = "绑定状态", readConverterExp = "0=占用中,1=已释放")
    private String status;

    /**
     * 获取主键ID
     *
     * @return 主键ID
     */
    public Long getId()
    {
        return id;
    }

    /**
     * 设置主键ID
     *
     * @param id 主键ID
     */
    public void setId(Long id)
    {
        this.id = id;
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
     * 获取设备ID
     *
     * @return 设备ID
     */
    public Long getDeviceId()
    {
        return deviceId;
    }

    /**
     * 设置设备ID
     *
     * @param deviceId 设备ID
     */
    public void setDeviceId(Long deviceId)
    {
        this.deviceId = deviceId;
    }

    /**
     * 获取设备编号
     *
     * @return 设备编号
     */
    public String getDeviceCode()
    {
        return deviceCode;
    }

    /**
     * 设置设备编号
     *
     * @param deviceCode 设备编号
     */
    public void setDeviceCode(String deviceCode)
    {
        this.deviceCode = deviceCode;
    }

    /**
     * 获取宿主机节点名
     *
     * @return 宿主机节点名
     */
    public String getNodeName()
    {
        return nodeName;
    }

    /**
     * 设置宿主机节点名
     *
     * @param nodeName 宿主机节点名
     */
    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
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
     * 获取绑定时间
     *
     * @return 绑定时间
     */
    public Date getBindTime()
    {
        return bindTime;
    }

    /**
     * 设置绑定时间
     *
     * @param bindTime 绑定时间
     */
    public void setBindTime(Date bindTime)
    {
        this.bindTime = bindTime;
    }

    /**
     * 获取释放时间
     *
     * @return 释放时间
     */
    public Date getReleaseTime()
    {
        return releaseTime;
    }

    /**
     * 设置释放时间
     *
     * @param releaseTime 释放时间
     */
    public void setReleaseTime(Date releaseTime)
    {
        this.releaseTime = releaseTime;
    }

    /**
     * 获取绑定状态
     *
     * @return 绑定状态
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * 设置绑定状态
     *
     * @param status 绑定状态
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * 重写toString方法，便于日志输出
     *
     * @return 对象字符串
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("allocId", getAllocId())
            .append("applyId", getApplyId())
            .append("deviceId", getDeviceId())
            .append("deviceCode", getDeviceCode())
            .append("nodeName", getNodeName())
            .append("resourceId", getResourceId())
            .append("bindTime", getBindTime())
            .append("releaseTime", getReleaseTime())
            .append("status", getStatus())
            .toString();
    }
}
