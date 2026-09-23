package com.ruoyi.bocompute.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.bocompute.domain.BizResource;

/**
 * 算力资源 数据层
 * 
 * @author bocloud
 */
public interface BizResourceMapper extends BaseMapper<BizResource>
{
    /**
     * 查询算力资源数据集合
     * 
     * @param bizResource 算力资源信息
     * @return 算力资源数据集合
     */
    public IPage<BizResource> selectBizResourceList(IPage<BizResource> page, @Param("bizResource") BizResource bizResource);

    /**
     * 查询算力资源列表（仅可用状态下拉选择用）
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
     * @param resourceCode 资源编码
     * @return 结果
     */
    public BizResource checkResourceCodeUnique(String resourceCode);

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
     * 占用资源数量（审批通过时调用，带可用数量校验，防止超卖）
     * 
     * @param resourceId 资源ID
     * @param count 占用数量
     * @return 结果
     */
    public int occupyResource(@Param("resourceId") Long resourceId, @Param("count") Integer count);

    /**
     * 释放资源数量（资源释放时调用）
     * 
     * @param resourceId 资源ID
     * @param count 释放数量
     * @return 结果
     */
    public int releaseResource(@Param("resourceId") Long resourceId, @Param("count") Integer count);

    /**
     * 按设备汇总回写资源池库存（设备是真相源）
     * total = 未删除设备数；used = 已分配设备数；available = 空闲设备数
     * 故障、维护设备只计入 total
     *
     * @param resourceId 资源池ID
     * @return 结果
     */
    public int syncResourceCount(Long resourceId);

    /**
     * 批量删除算力资源信息
     * 
     * @param resourceIds 需要删除的资源ID
     * @return 结果
     */
    public int deleteBizResourceByIds(Long[] resourceIds);

    /**
     * 删除算力资源信息
     * 
     * @param resourceId 资源ID
     * @return 结果
     */
    public int deleteBizResourceById(Long resourceId);
}
