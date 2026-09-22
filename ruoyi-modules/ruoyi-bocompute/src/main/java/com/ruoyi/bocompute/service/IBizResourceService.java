package com.ruoyi.bocompute.service;

import java.util.List;
import com.ruoyi.bocompute.domain.BizResource;

/**
 * 算力资源 服务层
 * 
 * @author bocloud
 */
public interface IBizResourceService
{
    /**
     * 查询算力资源信息集合
     * 
     * @param bizResource 算力资源信息
     * @return 算力资源列表
     */
    public List<BizResource> selectBizResourceList(BizResource bizResource);

    /**
     * 查询所有可用算力资源
     * 
     * @return 算力资源列表
     */
    public List<BizResource> selectBizResourceAll();

    /**
     * 通过资源ID查询算力资源信息
     * 
     * @param resourceId 资源ID
     * @return 算力资源信息
     */
    public BizResource selectBizResourceById(Long resourceId);

    /**
     * 校验资源编码是否唯一
     * 
     * @param bizResource 算力资源信息
     * @return 结果
     */
    public boolean checkResourceCodeUnique(BizResource bizResource);

    /**
     * 新增算力资源信息
     * 
     * @param bizResource 算力资源信息
     * @return 结果
     */
    public int insertBizResource(BizResource bizResource);

    /**
     * 修改算力资源信息
     * 
     * @param bizResource 算力资源信息
     * @return 结果
     */
    public int updateBizResource(BizResource bizResource);

    /**
     * 批量删除算力资源信息
     * 
     * @param resourceIds 需要删除的资源ID
     * @return 结果
     */
    public int deleteBizResourceByIds(Long[] resourceIds);

    /**
     * 占用资源数量（审批通过时调用，带可用数量校验，防止超卖）
     * 
     * @param resourceId 资源ID
     * @param count 占用数量
     * @return 结果
     */
    public int occupyResource(Long resourceId, Integer count);

    /**
     * 释放资源数量（资源释放时调用）
     * 
     * @param resourceId 资源ID
     * @param count 释放数量
     * @return 结果
     */
    public int releaseResource(Long resourceId, Integer count);
}
