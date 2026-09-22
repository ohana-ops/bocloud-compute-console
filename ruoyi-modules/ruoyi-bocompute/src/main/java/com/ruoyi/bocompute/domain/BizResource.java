package com.ruoyi.bocompute.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.annotation.Excel.ColumnType;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 算力资源对象 biz_resource
 * 
 * @author bocloud
 */
public class BizResource extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 资源ID */
    @Excel(name = "资源ID", cellType = ColumnType.NUMERIC)
    private Long resourceId;

    /** 资源名称 */
    @Excel(name = "资源名称")
    private String resourceName;

    /** 资源编码（唯一） */
    @Excel(name = "资源编码")
    private String resourceCode;

    /** 资源类型（gpu GPU卡 / npu 国产加速卡 / node 计算节点 / cpu CPU资源） */
    @Excel(name = "资源类型", readConverterExp = "gpu=GPU卡,npu=国产加速卡,node=计算节点,cpu=CPU资源")
    private String resourceType;

    /** 资源规格 */
    @Excel(name = "资源规格")
    private String resourceSpec;

    /** 所属集群名称 */
    @Excel(name = "所属集群")
    private String clusterName;

    /** 资源总数量 */
    @Excel(name = "总数量", cellType = ColumnType.NUMERIC)
    private Integer totalCount;

    /** 已分配数量 */
    @Excel(name = "已分配数量", cellType = ColumnType.NUMERIC)
    private Integer usedCount;

    /** 可用数量 */
    @Excel(name = "可用数量", cellType = ColumnType.NUMERIC)
    private Integer availableCount;

    /** 计量单位 */
    @Excel(name = "计量单位")
    private String unit;

    /** 资源状态（0可用 1停用 2维护中） */
    @Excel(name = "资源状态", readConverterExp = "0=可用,1=停用,2=维护中")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

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
    @NotBlank(message = "资源名称不能为空")
    @Size(min = 0, max = 64, message = "资源名称长度不能超过64个字符")
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
     * 获取资源编码
     *
     * @return 资源编码
     */
    @NotBlank(message = "资源编码不能为空")
    @Size(min = 0, max = 64, message = "资源编码长度不能超过64个字符")
    public String getResourceCode()
    {
        return resourceCode;
    }

    /**
     * 设置资源编码
     *
     * @param resourceCode 资源编码
     */
    public void setResourceCode(String resourceCode)
    {
        this.resourceCode = resourceCode;
    }

    /**
     * 获取资源类型
     *
     * @return 资源类型
     */
    @NotBlank(message = "资源类型不能为空")
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
     * 获取所属集群名称
     *
     * @return 所属集群名称
     */
    public String getClusterName()
    {
        return clusterName;
    }

    /**
     * 设置所属集群名称
     *
     * @param clusterName 所属集群名称
     */
    public void setClusterName(String clusterName)
    {
        this.clusterName = clusterName;
    }

    /**
     * 获取资源总数量
     *
     * @return 资源总数量
     */
    @NotNull(message = "资源总数量不能为空")
    public Integer getTotalCount()
    {
        return totalCount;
    }

    /**
     * 设置资源总数量
     *
     * @param totalCount 资源总数量
     */
    public void setTotalCount(Integer totalCount)
    {
        this.totalCount = totalCount;
    }

    /**
     * 获取已分配数量
     *
     * @return 已分配数量
     */
    public Integer getUsedCount()
    {
        return usedCount;
    }

    /**
     * 设置已分配数量
     *
     * @param usedCount 已分配数量
     */
    public void setUsedCount(Integer usedCount)
    {
        this.usedCount = usedCount;
    }

    /**
     * 获取可用数量
     *
     * @return 可用数量
     */
    public Integer getAvailableCount()
    {
        return availableCount;
    }

    /**
     * 设置可用数量
     *
     * @param availableCount 可用数量
     */
    public void setAvailableCount(Integer availableCount)
    {
        this.availableCount = availableCount;
    }

    /**
     * 获取计量单位
     *
     * @return 计量单位
     */
    public String getUnit()
    {
        return unit;
    }

    /**
     * 设置计量单位
     *
     * @param unit 计量单位
     */
    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    /**
     * 获取资源状态
     *
     * @return 资源状态
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * 设置资源状态
     *
     * @param status 资源状态
     */
    public void setStatus(String status)
    {
        this.status = status;
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
            .append("resourceId", getResourceId())
            .append("resourceName", getResourceName())
            .append("resourceCode", getResourceCode())
            .append("resourceType", getResourceType())
            .append("resourceSpec", getResourceSpec())
            .append("clusterName", getClusterName())
            .append("totalCount", getTotalCount())
            .append("usedCount", getUsedCount())
            .append("availableCount", getAvailableCount())
            .append("unit", getUnit())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
