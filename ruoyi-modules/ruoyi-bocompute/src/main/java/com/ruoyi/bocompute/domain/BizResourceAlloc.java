package com.ruoyi.bocompute.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.annotation.Excel.ColumnType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 资源分配记录对象 biz_resource_alloc
 * 
 * @author bocloud
 */
@TableName("biz_resource_alloc")
public class BizResourceAlloc extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分配记录ID */
    @TableId(value = "alloc_id", type = IdType.AUTO)
    @Excel(name = "分配记录ID", cellType = ColumnType.NUMERIC)
    private Long allocId;

    /** 关联申请单ID */
    private Long applyId;

    /** 申请单编号（冗余字段） */
    @Excel(name = "申请单编号")
    private String applyNo;

    /** 资源ID */
    private Long resourceId;

    /** 资源名称（冗余字段） */
    @Excel(name = "资源名称")
    private String resourceName;

    /** 本次分配数量 */
    @Excel(name = "分配数量", cellType = ColumnType.NUMERIC)
    private Integer allocCount;

    /** 资源使用人ID */
    private Long userId;

    /** 资源使用人账号 */
    @Excel(name = "使用人")
    private String userName;

    /** 使用人部门ID */
    private Long deptId;

    /** 使用人部门名称 */
    @Excel(name = "使用部门")
    private String deptName;

    /** 使用开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;

    /** 使用结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 使用状态（0使用中 1已释放） */
    @Excel(name = "使用状态", readConverterExp = "0=使用中,1=已释放")
    private String status;

    /** 释放时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "释放时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date releaseTime;

    /** 绑定设备编号，逗号拼接，列表展示用 */
    @Excel(name = "绑定设备")
    private String deviceCodes;

    /** 涉及的节点，逗号分隔 */
    @Excel(name = "涉及节点")
    private String nodeNames;

    /** 调度实现标识（device-pool/k8s） */
    @Excel(name = "调度实现")
    private String scheduleType;

    /** 调度结果说明 */
    @Excel(name = "调度说明")
    private String scheduleMsg;

    /** 删除标志（0代表存在 2代表删除） */
    @TableLogic(value = "0", delval = "2")
    private String delFlag;

    /**
     * 获取分配记录ID
     *
     * @return 分配记录ID
     */
    public Long getAllocId()
    {
        return allocId;
    }

    /**
     * 设置分配记录ID
     *
     * @param allocId 分配记录ID
     */
    public void setAllocId(Long allocId)
    {
        this.allocId = allocId;
    }

    /**
     * 获取关联申请单ID
     *
     * @return 关联申请单ID
     */
    public Long getApplyId()
    {
        return applyId;
    }

    /**
     * 设置关联申请单ID
     *
     * @param applyId 关联申请单ID
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
     * 获取资源ID
     *
     * @return 资源ID
     */
    public Long getResourceId()
    {
        return resourceId;
    }

    /**
     * 设置资源ID
     *
     * @param resourceId 资源ID
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
     * 获取本次分配数量
     *
     * @return 本次分配数量
     */
    public Integer getAllocCount()
    {
        return allocCount;
    }

    /**
     * 设置本次分配数量
     *
     * @param allocCount 本次分配数量
     */
    public void setAllocCount(Integer allocCount)
    {
        this.allocCount = allocCount;
    }

    /**
     * 获取资源使用人ID
     *
     * @return 资源使用人ID
     */
    public Long getUserId()
    {
        return userId;
    }

    /**
     * 设置资源使用人ID
     *
     * @param userId 资源使用人ID
     */
    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    /**
     * 获取资源使用人账号
     *
     * @return 资源使用人账号
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * 设置资源使用人账号
     *
     * @param userName 资源使用人账号
     */
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    /**
     * 获取使用人部门ID
     *
     * @return 使用人部门ID
     */
    public Long getDeptId()
    {
        return deptId;
    }

    /**
     * 设置使用人部门ID
     *
     * @param deptId 使用人部门ID
     */
    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    /**
     * 获取使用人部门名称
     *
     * @return 使用人部门名称
     */
    public String getDeptName()
    {
        return deptName;
    }

    /**
     * 设置使用人部门名称
     *
     * @param deptName 使用人部门名称
     */
    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
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
     * 获取使用状态
     *
     * @return 使用状态
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * 设置使用状态
     *
     * @param status 使用状态
     */
    public void setStatus(String status)
    {
        this.status = status;
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
     * 获取绑定设备编号
     *
     * @return 绑定设备编号
     */
    public String getDeviceCodes()
    {
        return deviceCodes;
    }

    /**
     * 设置绑定设备编号
     *
     * @param deviceCodes 绑定设备编号
     */
    public void setDeviceCodes(String deviceCodes)
    {
        this.deviceCodes = deviceCodes;
    }

    /**
     * 获取涉及的节点
     *
     * @return 涉及的节点
     */
    public String getNodeNames()
    {
        return nodeNames;
    }

    /**
     * 设置涉及的节点
     *
     * @param nodeNames 涉及的节点
     */
    public void setNodeNames(String nodeNames)
    {
        this.nodeNames = nodeNames;
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
    public String getScheduleMsg()
    {
        return scheduleMsg;
    }

    /**
     * 设置调度结果说明
     *
     * @param scheduleMsg 调度结果说明
     */
    public void setScheduleMsg(String scheduleMsg)
    {
        this.scheduleMsg = scheduleMsg;
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
            .append("allocId", getAllocId())
            .append("applyId", getApplyId())
            .append("applyNo", getApplyNo())
            .append("resourceId", getResourceId())
            .append("resourceName", getResourceName())
            .append("allocCount", getAllocCount())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("deptId", getDeptId())
            .append("deptName", getDeptName())
            .append("beginTime", getBeginTime())
            .append("endTime", getEndTime())
            .append("status", getStatus())
            .append("releaseTime", getReleaseTime())
            .append("deviceCodes", getDeviceCodes())
            .append("nodeNames", getNodeNames())
            .append("scheduleType", getScheduleType())
            .append("scheduleMsg", getScheduleMsg())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
