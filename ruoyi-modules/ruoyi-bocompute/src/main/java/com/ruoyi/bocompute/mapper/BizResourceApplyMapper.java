package com.ruoyi.bocompute.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.bocompute.domain.BizResourceApply;

/**
 * 资源申请 数据层
 * 
 * @author bocloud
 */
public interface BizResourceApplyMapper extends BaseMapper<BizResourceApply>
{
    /**
     * 查询资源申请数据集合
     * 
     * @param bizResourceApply 资源申请信息
     * @return 资源申请数据集合
     */
    public IPage<BizResourceApply> selectBizResourceApplyList(IPage<BizResourceApply> page, @Param("bizResourceApply") BizResourceApply bizResourceApply);

    /**
     * 通过申请单ID查询资源申请信息
     * 
     * @param applyId 申请单ID
     * @return 资源申请信息
     */
    public BizResourceApply selectBizResourceApplyById(Long applyId);

    /**
     * 查询待审批申请单数量
     * 
     * @return 待审批数量
     */
    public int countPendingApply();

    /**
     * 查询本月申请单数量
     * 
     * @return 本月申请单数量
     */
    public int countCurrentMonthApply();

    /**
     * 新增资源申请信息
     * 
     * @param bizResourceApply 资源申请信息
     * @return 结果
     */
    public int insertBizResourceApply(BizResourceApply bizResourceApply);

    /**
     * 修改资源申请信息
     * 
     * @param bizResourceApply 资源申请信息
     * @return 结果
     */
    public int updateBizResourceApply(BizResourceApply bizResourceApply);

    /**
     * 批量删除资源申请信息
     * 
     * @param applyIds 需要删除的申请单ID
     * @return 结果
     */
    public int deleteBizResourceApplyByIds(Long[] applyIds);

    /**
     * 删除资源申请信息
     * 
     * @param applyId 申请单ID
     * @return 结果
     */
    public int deleteBizResourceApplyById(Long applyId);
}
