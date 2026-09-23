package com.ruoyi.bocompute.service;

import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.bocompute.domain.BizResourceAlloc;

/**
 * 资源分配记录 服务层
 * 
 * @author bocloud
 */
public interface IBizResourceAllocService
{
    /**
     * 查询资源分配记录信息集合
     * 
     * @param bizResourceAlloc 资源分配记录信息
     * @return 资源分配记录列表
     */
    public IPage<BizResourceAlloc> selectBizResourceAllocList(IPage<BizResourceAlloc> page, BizResourceAlloc bizResourceAlloc);

    /**
     * 通过分配记录ID查询资源分配记录信息
     * 
     * @param allocId 分配记录ID
     * @return 资源分配记录信息
     */
    public BizResourceAlloc selectBizResourceAllocById(Long allocId);

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
     * 释放资源（回收已分配资源并回写资源可用数量）
     * 
     * @param allocId 分配记录ID
     * @return 结果
     */
    public int releaseAlloc(Long allocId);
}
