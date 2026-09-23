package com.ruoyi.bocompute.domain;

import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.annotation.Excel.ColumnType;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 模拟算力设备对象 biz_device
 *
 * @author bocloud
 */
public class BizDevice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 设备状态：空闲 */
    public static final String STATUS_IDLE = "0";

    /** 设备状态：已分配 */
    public static final String STATUS_ALLOCATED = "1";

    /** 设备状态：故障 */
    public static final String STATUS_FAULT = "2";

    /** 设备状态：维护 */
    public static final String STATUS_MAINTAIN = "3";

    /** 设备ID */
    @Excel(name = "设备ID", cellType = ColumnType.NUMERIC)
    private Long deviceId;

    /** 设备编号（唯一） */
    @Excel(name = "设备编号")
    private String deviceCode;

    /** 所属资源池ID */
    private Long resourceId;

    /** 资源名称（非表字段，列表展示用） */
    @Excel(name = "资源池")
    private String resourceName;

    /** 冗余资源类型（gpu/npu/node） */
    @Excel(name = "资源类型", readConverterExp = "gpu=GPU卡,npu=国产加速卡,node=计算节点")
    private String resourceType;

    /** 冗余资源规格 */
    @Excel(name = "资源规格")
    private String resourceSpec;

    /** 所属集群 */
    @Excel(name = "集群")
    private String clusterName;

    /** 宿主机节点名 */
    @Excel(name = "节点")
    private String nodeName;

    /** 节点内槽位 */
    @Excel(name = "槽位", cellType = ColumnType.NUMERIC)
    private Integer slotIndex;

    /** 模拟 UUID */
    private String uuid;

    /** 模拟 PCI 总线号 */
    @Excel(name = "PCI")
    private String pciBus;

    /** 设备状态（0空闲 1已分配 2故障 3维护） */
    @Excel(name = "设备状态", readConverterExp = "0=空闲,1=已分配,2=故障,3=维护")
    private String status;

    /** 当前绑定的分配单ID */
    private Long allocId;

    /** 当前申请单号 */
    @Excel(name = "申请单号")
    private String applyNo;

    /** 当前使用人 */
    @Excel(name = "当前使用人")
    private String userName;

    /** 本次绑定时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "绑定时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date bindTime;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

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
    @NotBlank(message = "设备编号不能为空")
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
     * 获取所属资源池ID
     *
     * @return 所属资源池ID
     */
    @NotNull(message = "所属资源池不能为空")
    public Long getResourceId()
    {
        return resourceId;
    }

    /**
     * 设置所属资源池ID
     *
     * @param resourceId 所属资源池ID
     */
    public void setResourceId(Long resourceId)
    {
        this.resourceId = resourceId;
    }

    /**
     * 获取资源名称
     *
     * @return 资源名称
     */
    public String getResourceName()
    {
        return resourceName;
    }

    /**
     * 设置资源名称
     *
     * @param resourceName 资源名称
     */
    public void setResourceName(String resourceName)
    {
        this.resourceName = resourceName;
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
     * 获取资源规格
     *
     * @return 资源规格
     */
    public String getResourceSpec()
    {
        return resourceSpec;
    }

    /**
     * 设置资源规格
     *
     * @param resourceSpec 资源规格
     */
    public void setResourceSpec(String resourceSpec)
    {
        this.resourceSpec = resourceSpec;
    }

    /**
     * 获取所属集群
     *
     * @return 所属集群
     */
    public String getClusterName()
    {
        return clusterName;
    }

    /**
     * 设置所属集群
     *
     * @param clusterName 所属集群
     */
    public void setClusterName(String clusterName)
    {
        this.clusterName = clusterName;
    }

    /**
     * 获取宿主机节点名
     *
     * @return 宿主机节点名
     */
    @NotBlank(message = "宿主机节点名不能为空")
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
     * 获取节点内槽位
     *
     * @return 节点内槽位
     */
    public Integer getSlotIndex()
    {
        return slotIndex;
    }

    /**
     * 设置节点内槽位
     *
     * @param slotIndex 节点内槽位
     */
    public void setSlotIndex(Integer slotIndex)
    {
        this.slotIndex = slotIndex;
    }

    /**
     * 获取模拟UUID
     *
     * @return 模拟UUID
     */
    public String getUuid()
    {
        return uuid;
    }

    /**
     * 设置模拟UUID
     *
     * @param uuid 模拟UUID
     */
    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    /**
     * 获取模拟PCI总线号
     *
     * @return 模拟PCI总线号
     */
    public String getPciBus()
    {
        return pciBus;
    }

    /**
     * 设置模拟PCI总线号
     *
     * @param pciBus 模拟PCI总线号
     */
    public void setPciBus(String pciBus)
    {
        this.pciBus = pciBus;
    }

    /**
     * 获取设备状态
     *
     * @return 设备状态
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * 设置设备状态
     *
     * @param status 设备状态
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * 获取当前绑定的分配单ID
     *
     * @return 当前绑定的分配单ID
     */
    public Long getAllocId()
    {
        return allocId;
    }

    /**
     * 设置当前绑定的分配单ID
     *
     * @param allocId 当前绑定的分配单ID
     */
    public void setAllocId(Long allocId)
    {
        this.allocId = allocId;
    }

    /**
     * 获取当前申请单号
     *
     * @return 当前申请单号
     */
    public String getApplyNo()
    {
        return applyNo;
    }

    /**
     * 设置当前申请单号
     *
     * @param applyNo 当前申请单号
     */
    public void setApplyNo(String applyNo)
    {
        this.applyNo = applyNo;
    }

    /**
     * 获取当前使用人
     *
     * @return 当前使用人
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * 设置当前使用人
     *
     * @param userName 当前使用人
     */
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    /**
     * 获取本次绑定时间
     *
     * @return 本次绑定时间
     */
    public Date getBindTime()
    {
        return bindTime;
    }

    /**
     * 设置本次绑定时间
     *
     * @param bindTime 本次绑定时间
     */
    public void setBindTime(Date bindTime)
    {
        this.bindTime = bindTime;
    }

    /**
     * 获取删除标志
     *
     * @return 删除标志
     */
    public String getDelFlag()
    {
        return delFlag;
    }

    /**
     * 设置删除标志
     *
     * @param delFlag 删除标志
     */
    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    /**
     * 重写toString方法，便于日志输出
     *
     * @return 对象字符串
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("deviceId", getDeviceId())
            .append("deviceCode", getDeviceCode())
            .append("resourceId", getResourceId())
            .append("resourceName", getResourceName())
            .append("resourceType", getResourceType())
            .append("resourceSpec", getResourceSpec())
            .append("clusterName", getClusterName())
            .append("nodeName", getNodeName())
            .append("slotIndex", getSlotIndex())
            .append("uuid", getUuid())
            .append("pciBus", getPciBus())
            .append("status", getStatus())
            .append("allocId", getAllocId())
            .append("applyNo", getApplyNo())
            .append("userName", getUserName())
            .append("bindTime", getBindTime())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
