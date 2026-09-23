package com.ruoyi.bocompute.domain;

import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.annotation.Excel.ColumnType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 资源申请对象 biz_resource_apply
 * 
 * @author bocloud
 */
@TableName("biz_resource_apply")
public class BizResourceApply extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 申请单ID */
    @TableId(value = "apply_id", type = IdType.AUTO)
    @Excel(name = "申请单ID", cellType = ColumnType.NUMERIC)
    private Long applyId;

    /** 申请单编号（业务唯一号） */
    @Excel(name = "申请单编号")
    private String applyNo;

    /** 申请的资源ID */
    private Long resourceId;

    /** 资源名称（冗余字段） */
    @Excel(name = "资源名称")
    private String resourceName;

    /** 资源类型（冗余字段） */
    @Excel(name = "资源类型", readConverterExp = "gpu=GPU卡,npu=国产加速卡,node=计算节点,cpu=CPU资源")
    private String resourceType;

    /** 申请数量 */
    @Excel(name = "申请数量", cellType = ColumnType.NUMERIC)
    private Integer applyCount;

    /** 申请人ID */
    private Long applyUserId;

    /** 申请人账号 */
    @Excel(name = "申请人")
    private String applyUserName;

    /** 申请人部门ID */
    private Long applyDeptId;

    /** 申请人部门名称 */
    @Excel(name = "申请部门")
    private String applyDeptName;

    /** 申请用途 */
    @Excel(name = "申请用途")
    private String purpose;

    /** 使用开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;

    /** 使用结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 申请状态（0待审批 1已通过 2已驳回 3已取消） */
    @Excel(name = "申请状态", readConverterExp = "0=待审批,1=已通过,2=已驳回,3=已取消")
    private String status;

    /** 审批人账号 */
    @Excel(name = "审批人")
    private String auditBy;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审批时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审批意见 */
    @Excel(name = "审批意见")
    private String auditOpinion;

    /** 绑定设备编号，逗号拼接（非表字段，由分配记录组装后返回，便于前端展示） */
    @TableField(exist = false)
    private String deviceCodes;

    /** 涉及的节点，逗号分隔（非表字段） */
    @TableField(exist = false)
    private String nodeNames;

    /** 删除标志（0代表存在 2代表删除） */
    @TableLogic(value = "0", delval = "2")
    private String delFlag;

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
     * 获取申请的资源ID
     *
     * @return 申请的资源ID
     */
    @NotNull(message = "申请的资源不能为空")
    public Long getResourceId()
    {
        return resourceId;
    }

    /**
     * 设置申请的资源ID
     *
     * @param resourceId 申请的资源ID
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
     * 获取申请数量
     *
     * @return 申请数量
     */
    @NotNull(message = "申请数量不能为空")
    public Integer getApplyCount()
    {
        return applyCount;
    }

    /**
     * 设置申请数量
     *
     * @param applyCount 申请数量
     */
    public void setApplyCount(Integer applyCount)
    {
        this.applyCount = applyCount;
    }

    /**
     * 获取申请人ID
     *
     * @return 申请人ID
     */
    public Long getApplyUserId()
    {
        return applyUserId;
    }

    /**
     * 设置申请人ID
     *
     * @param applyUserId 申请人ID
     */
    public void setApplyUserId(Long applyUserId)
    {
        this.applyUserId = applyUserId;
    }

    /**
     * 获取申请人账号
     *
     * @return 申请人账号
     */
    public String getApplyUserName()
    {
        return applyUserName;
    }

    /**
     * 设置申请人账号
     *
     * @param applyUserName 申请人账号
     */
    public void setApplyUserName(String applyUserName)
    {
        this.applyUserName = applyUserName;
    }

    /**
     * 获取申请人部门ID
     *
     * @return 申请人部门ID
     */
    public Long getApplyDeptId()
    {
        return applyDeptId;
    }

    /**
     * 设置申请人部门ID
     *
     * @param applyDeptId 申请人部门ID
     */
    public void setApplyDeptId(Long applyDeptId)
    {
        this.applyDeptId = applyDeptId;
    }

    /**
     * 获取申请人部门名称
     *
     * @return 申请人部门名称
     */
    public String getApplyDeptName()
    {
        return applyDeptName;
    }

    /**
     * 设置申请人部门名称
     *
     * @param applyDeptName 申请人部门名称
     */
    public void setApplyDeptName(String applyDeptName)
    {
        this.applyDeptName = applyDeptName;
    }

    /**
     * 获取申请用途
     *
     * @return 申请用途
     */
    @NotBlank(message = "申请用途不能为空")
    @Size(min = 0, max = 500, message = "申请用途长度不能超过500个字符")
    public String getPurpose()
    {
        return purpose;
    }

    /**
     * 设置申请用途
     *
     * @param purpose 申请用途
     */
    public void setPurpose(String purpose)
    {
        this.purpose = purpose;
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
     * 获取申请状态
     *
     * @return 申请状态
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * 设置申请状态
     *
     * @param status 申请状态
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * 获取审批人账号
     *
     * @return 审批人账号
     */
    public String getAuditBy()
    {
        return auditBy;
    }

    /**
     * 设置审批人账号
     *
     * @param auditBy 审批人账号
     */
    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    /**
     * 获取审批时间
     *
     * @return 审批时间
     */
    public Date getAuditTime()
    {
        return auditTime;
    }

    /**
     * 设置审批时间
     *
     * @param auditTime 审批时间
     */
    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    /**
     * 获取审批意见
     *
     * @return 审批意见
     */
    public String getAuditOpinion()
    {
        return auditOpinion;
    }

    /**
     * 设置审批意见
     *
     * @param auditOpinion 审批意见
     */
    public void setAuditOpinion(String auditOpinion)
    {
        this.auditOpinion = auditOpinion;
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
            .append("applyId", getApplyId())
            .append("applyNo", getApplyNo())
            .append("resourceId", getResourceId())
            .append("resourceName", getResourceName())
            .append("resourceType", getResourceType())
            .append("applyCount", getApplyCount())
            .append("applyUserId", getApplyUserId())
            .append("applyUserName", getApplyUserName())
            .append("applyDeptId", getApplyDeptId())
            .append("applyDeptName", getApplyDeptName())
            .append("purpose", getPurpose())
            .append("beginTime", getBeginTime())
            .append("endTime", getEndTime())
            .append("status", getStatus())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("auditOpinion", getAuditOpinion())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
