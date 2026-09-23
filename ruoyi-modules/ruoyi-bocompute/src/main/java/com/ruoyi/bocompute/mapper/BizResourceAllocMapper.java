package com.ruoyi.bocompute.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.bocompute.domain.BizResourceAlloc;

/**
 * 资源分配记录 数据层
 * 
 * @author bocloud
 */
public interface BizResourceAllocMapper extends BaseMapper<BizResourceAlloc>
{
    /**
     * 查询资源分配记录数据集合
     * 
     * @param bizResourceAlloc 资源分配记录信息
     * @return 资源分配记录数据集合
     */
    public IPage<BizResourceAlloc> selectBizResourceAllocList(IPage<BizResourceAlloc> page, @Param("bizResourceAlloc") BizResourceAlloc bizResourceAlloc);

    /**
     * 通过分配记录ID查询资源分配记录信息
     * 
     * @param allocId 分配记录ID
     * @return 资源分配记录信息
     */
    public BizResourceAlloc selectBizResourceAllocById(Long allocId);

    /**
     * 通过申请单ID查询分配记录信息
     * 
     * @param applyId 申请单ID
     * @return 分配记录信息
     */
    public BizResourceAlloc selectBizResourceAllocByApplyId(Long applyId);

    /**
     * 查询已到期仍未释放的分配记录（定时任务使用）
     * 条件：未删除、使用中、结束时间不为空且已早于当前时间
     *
     * @return 到期的分配记录集合
     */
    public List<BizResourceAlloc> selectExpireAllocList();

    /**
     * 新增资源分配记录信息
     * 
     * @param bizResourceAlloc 资源分配记录信息
     * @return 结果
     */
    public int insertBizResourceAlloc(BizResourceAlloc bizResourceAlloc);

    /**
     * 修改资源分配记录信息
     * 
     * @param bizResourceAlloc 资源分配记录信息
     * @return 结果
     */
    public int updateBizResourceAlloc(BizResourceAlloc bizResourceAlloc);

    /**
     * 批量删除资源分配记录信息
     * 
     * @param allocIds 需要删除的分配记录ID
     * @return 结果
     */
    public int deleteBizResourceAllocByIds(Long[] allocIds);

    /**
     * 删除资源分配记录信息
     * 
     * @param allocId 分配记录ID
     * @return 结果
     */
    public int deleteBizResourceAllocById(Long allocId);
}
