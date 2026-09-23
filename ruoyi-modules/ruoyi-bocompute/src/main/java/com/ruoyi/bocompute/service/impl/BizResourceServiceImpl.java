package com.ruoyi.bocompute.service.impl;

import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.bocompute.domain.BizResource;
import com.ruoyi.bocompute.mapper.BizDeviceMapper;
import com.ruoyi.bocompute.mapper.BizResourceMapper;
import com.ruoyi.bocompute.schedule.DeviceCodeGenerator;
import com.ruoyi.bocompute.service.IBizResourceService;

/**
 * 算力资源 服务层处理
 * 
 * @author bocloud
 */
@Service
public class BizResourceServiceImpl implements IBizResourceService
{
    @Autowired
    private BizResourceMapper bizResourceMapper;

    @Autowired
    private BizDeviceMapper bizDeviceMapper;

    /**
     * 分页查询算力资源信息集合
     * 
     * @param page 分页参数
     * @param bizResource 算力资源信息
     * @return 算力资源信息集合
     */
    @Override
    public IPage<BizResource> selectBizResourceList(IPage<BizResource> page, BizResource bizResource)
    {
        return bizResourceMapper.selectBizResourceList(page, bizResource);
    }

    /**
     * 查询所有可用算力资源
     * 
     * @return 算力资源列表
     */
    @Override
    public List<BizResource> selectBizResourceAll()
    {
        return bizResourceMapper.selectBizResourceAll();
    }

    /**
     * 通过资源ID查询算力资源信息
     *
     * @param resourceId 资源ID
     * @return 算力资源信息
     */
    @Override
    public BizResource selectBizResourceById(Long resourceId)
    {
        return bizResourceMapper.selectBizResourceById(resourceId);
    }

    /**
     * 校验资源编码是否唯一
     *
     * @param bizResource 算力资源信息
     * @return 结果
     */
    @Override
    public boolean checkResourceCodeUnique(BizResource bizResource)
    {
        Long resourceId = StringUtils.isNull(bizResource.getResourceId()) ? -1L : bizResource.getResourceId();
        BizResource info = bizResourceMapper.checkResourceCodeUnique(bizResource.getResourceCode());
        if (StringUtils.isNotNull(info) && info.getResourceId().longValue() != resourceId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 新增算力资源信息
     *
     * @param bizResource 算力资源信息
     * @return 结果
     */
    @Override
    public int insertBizResource(BizResource bizResource)
    {
        // 新增时可用数量默认等于总数量
        if (StringUtils.isNull(bizResource.getAvailableCount()))
        {
            bizResource.setAvailableCount(bizResource.getTotalCount());
        }
        // 新增时已分配数量默认为0
        if (StringUtils.isNull(bizResource.getUsedCount()))
        {
            bizResource.setUsedCount(0);
        }
        return bizResourceMapper.insertBizResource(bizResource);
    }

    /**
     * 修改算力资源信息
     *
     * @param bizResource 算力资源信息
     * @return 结果
     */
    @Override
    public int updateBizResource(BizResource bizResource)
    {
        // 走设备调度的资源类型，库存由设备状态汇总得出，禁止页面手改三个数量字段
        BizResource oldResource = bizResourceMapper.selectBizResourceById(bizResource.getResourceId());
        if (StringUtils.isNotNull(oldResource) && DeviceCodeGenerator.needDevice(oldResource.getResourceType()))
        {
            bizResource.setTotalCount(null);
            bizResource.setUsedCount(null);
            bizResource.setAvailableCount(null);
        }
        // 修改总数量时，同步重算可用数量（可用 = 总数 - 已分配），并校验不能小于已分配数量
        if (StringUtils.isNotNull(bizResource.getTotalCount()))
        {
            BizResource old = bizResourceMapper.selectBizResourceById(bizResource.getResourceId());
            Integer usedCount = StringUtils.isNull(old) || StringUtils.isNull(old.getUsedCount()) ? 0 : old.getUsedCount();
            if (bizResource.getTotalCount() < usedCount)
            {
                throw new ServiceException("资源总数量不能小于已分配数量" + usedCount);
            }
            bizResource.setUsedCount(usedCount);
            bizResource.setAvailableCount(bizResource.getTotalCount() - usedCount);
        }
        return bizResourceMapper.updateBizResource(bizResource);
    }

    /**
     * 批量删除算力资源信息
     *
     * @param resourceIds 需要删除的资源ID
     * @return 结果
     */
    @Override
    public int deleteBizResourceByIds(Long[] resourceIds)
    {
        // 删除前校验资源是否已被分配，已分配的资源不允许删除
        for (Long resourceId : resourceIds)
        {
            BizResource resource = bizResourceMapper.selectBizResourceById(resourceId);
            if (StringUtils.isNull(resource))
            {
                continue;
            }
            if (StringUtils.isNotNull(resource.getUsedCount()) && resource.getUsedCount() > 0)
            {
                throw new ServiceException(String.format("%1$s已分配,不能删除", resource.getResourceName()));
            }
            // 池下还有未删除的设备时禁止删池，避免设备变成孤儿数据
            int deviceCount = bizDeviceMapper.countDeviceByResourceId(resourceId);
            if (deviceCount > 0)
            {
                throw new ServiceException(String.format("%1$s下还有%2$s台设备,不能删除", resource.getResourceName(), deviceCount));
            }
        }
        return bizResourceMapper.deleteBizResourceByIds(resourceIds);
    }

    /**
     * 占用资源数量（审批通过时调用，带可用数量校验，防止超卖）
     *
     * @param resourceId 资源ID
     * @param count 占用数量
     * @return 结果
     */
    @Override
    public int occupyResource(Long resourceId, Integer count)
    {
        int rows = bizResourceMapper.occupyResource(resourceId, count);
        // 影响行数为0说明可用数量不足（并发情况下由 SQL 的 available_count >= count 条件兜底）
        if (rows == 0)
        {
            throw new ServiceException("资源可用数量不足，审批失败");
        }
        return rows;
    }

    /**
     * 释放资源数量（资源释放时调用）
     *
     * @param resourceId 资源ID
     * @param count 释放数量
     * @return 结果
     */
    @Override
    public int releaseResource(Long resourceId, Integer count)
    {
        return bizResourceMapper.releaseResource(resourceId, count);
    }
}
