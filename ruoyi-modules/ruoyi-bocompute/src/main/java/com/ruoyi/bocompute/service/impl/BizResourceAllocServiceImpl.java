package com.ruoyi.bocompute.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.bocompute.domain.BizResourceAlloc;
import com.ruoyi.bocompute.mapper.BizResourceAllocMapper;
import com.ruoyi.bocompute.mapper.BizResourceMapper;
import com.ruoyi.bocompute.service.IBizResourceAllocService;

/**
 * 资源分配记录 服务层处理
 * 
 * @author bocloud
 */
@Service
public class BizResourceAllocServiceImpl implements IBizResourceAllocService
{
    /** 分配状态：使用中 */
    private static final String STATUS_USING = "0";

    /** 分配状态：已释放 */
    private static final String STATUS_RELEASED = "1";

    @Autowired
    private BizResourceAllocMapper bizResourceAllocMapper;

    @Autowired
    private BizResourceMapper bizResourceMapper;

    /**
     * 查询资源分配记录信息集合
     * 
     * @param bizResourceAlloc 资源分配记录信息
     * @return 资源分配记录信息集合
     */
    @Override
    public List<BizResourceAlloc> selectBizResourceAllocList(BizResourceAlloc bizResourceAlloc)
    {
        return bizResourceAllocMapper.selectBizResourceAllocList(bizResourceAlloc);
    }

    /**
     * 通过分配记录ID查询资源分配记录信息
     *
     * @param allocId 分配记录ID
     * @return 资源分配记录信息
     */
    @Override
    public BizResourceAlloc selectBizResourceAllocById(Long allocId)
    {
        return bizResourceAllocMapper.selectBizResourceAllocById(allocId);
    }

    /**
     * 新增资源分配记录信息
     *
     * @param bizResourceAlloc 资源分配记录信息
     * @return 结果
     */
    @Override
    public int insertBizResourceAlloc(BizResourceAlloc bizResourceAlloc)
    {
        bizResourceAlloc.setCreateTime(DateUtils.getNowDate());
        return bizResourceAllocMapper.insertBizResourceAlloc(bizResourceAlloc);
    }

    /**
     * 修改资源分配记录信息
     *
     * @param bizResourceAlloc 资源分配记录信息
     * @return 结果
     */
    @Override
    public int updateBizResourceAlloc(BizResourceAlloc bizResourceAlloc)
    {
        bizResourceAlloc.setUpdateTime(DateUtils.getNowDate());
        return bizResourceAllocMapper.updateBizResourceAlloc(bizResourceAlloc);
    }

    /**
     * 批量删除资源分配记录信息
     *
     * @param allocIds 需要删除的分配记录ID
     * @return 结果
     */
    @Override
    public int deleteBizResourceAllocByIds(Long[] allocIds)
    {
        return bizResourceAllocMapper.deleteBizResourceAllocByIds(allocIds);
    }

    /**
     * 释放资源（回收已分配资源并回写资源可用数量）
     *
     * @param allocId 分配记录ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int releaseAlloc(Long allocId)
    {
        BizResourceAlloc alloc = bizResourceAllocMapper.selectBizResourceAllocById(allocId);
        if (StringUtils.isNull(alloc))
        {
            throw new ServiceException("分配记录不存在");
        }
        // 只有使用中的资源才允许释放
        if (!STATUS_USING.equals(alloc.getStatus()))
        {
            throw new ServiceException("该资源已释放，请勿重复操作");
        }
        // 1、回写资源可用数量（可用 + 本次释放数量，已分配 - 本次释放数量）
        bizResourceMapper.releaseResource(alloc.getResourceId(), alloc.getAllocCount());
        // 2、更新分配记录状态为已释放，并记录释放时间
        BizResourceAlloc update = new BizResourceAlloc();
        update.setAllocId(allocId);
        update.setStatus(STATUS_RELEASED);
        update.setReleaseTime(DateUtils.getNowDate());
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        return bizResourceAllocMapper.updateBizResourceAlloc(update);
    }
}
