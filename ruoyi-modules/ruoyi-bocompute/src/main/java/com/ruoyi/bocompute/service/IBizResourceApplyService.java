package com.ruoyi.bocompute.service;

import java.util.List;
import com.ruoyi.bocompute.domain.BizResourceApply;

/**
 * 资源申请 服务层
 * 
 * @author bocloud
 */
public interface IBizResourceApplyService
{
    /**
     * 查询资源申请信息集合
     * 
     * @param bizResourceApply 资源申请信息
     * @return 资源申请列表
     */
    public List<BizResourceApply> selectBizResourceApplyList(BizResourceApply bizResourceApply);

    /**
     * 通过申请单ID查询资源申请信息
     * 
     * @param applyId 申请单ID
     * @return 资源申请信息
     */
    public BizResourceApply selectBizResourceApplyById(Long applyId);

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
     * 取消申请（仅待审批状态可取消）
     * 
     * @param applyId 申请单ID
     * @return 结果
     */
    public int cancelApply(Long applyId);

    /**
     * 审批通过（占用资源并生成分配记录）
     * 
     * @param applyId 申请单ID
     * @param auditOpinion 审批意见
     * @return 结果
     */
    public int passApply(Long applyId, String auditOpinion);

    /**
     * 审批驳回
     * 
     * @param applyId 申请单ID
     * @param auditOpinion 审批意见
     * @return 结果
     */
    public int rejectApply(Long applyId, String auditOpinion);

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
}
