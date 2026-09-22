package com.ruoyi.bocompute.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.bocompute.domain.BizResource;
import com.ruoyi.bocompute.domain.BizResourceAlloc;
import com.ruoyi.bocompute.domain.BizResourceApply;
import com.ruoyi.bocompute.mapper.BizResourceAllocMapper;
import com.ruoyi.bocompute.mapper.BizResourceApplyMapper;
import com.ruoyi.bocompute.mapper.BizResourceMapper;
import com.ruoyi.bocompute.service.IBizResourceApplyService;

/**
 * 资源申请 服务层处理
 * 
 * @author bocloud
 */
@Service
public class BizResourceApplyServiceImpl implements IBizResourceApplyService
{
    /** 申请状态：待审批 */
    private static final String STATUS_PENDING = "0";

    /** 申请状态：已通过 */
    private static final String STATUS_PASS = "1";

    /** 申请状态：已驳回 */
    private static final String STATUS_REJECT = "2";

    /** 申请状态：已取消 */
    private static final String STATUS_CANCEL = "3";

    @Autowired
    private BizResourceApplyMapper bizResourceApplyMapper;

    @Autowired
    private BizResourceMapper bizResourceMapper;

    @Autowired
    private BizResourceAllocMapper bizResourceAllocMapper;

    /**
     * 查询资源申请信息集合
     * 
     * @param bizResourceApply 资源申请信息
     * @return 资源申请信息集合
     */
    @Override
    public List<BizResourceApply> selectBizResourceApplyList(BizResourceApply bizResourceApply)
    {
        return bizResourceApplyMapper.selectBizResourceApplyList(bizResourceApply);
    }

    /**
     * 通过申请单ID查询资源申请信息
     *
     * @param applyId 申请单ID
     * @return 资源申请信息
     */
    @Override
    public BizResourceApply selectBizResourceApplyById(Long applyId)
    {
        return bizResourceApplyMapper.selectBizResourceApplyById(applyId);
    }

    /**
     * 新增资源申请信息
     *
     * @param bizResourceApply 资源申请信息
     * @return 结果
     */
    @Override
    public int insertBizResourceApply(BizResourceApply bizResourceApply)
    {
        // 校验资源是否存在
        BizResource resource = bizResourceMapper.selectBizResourceById(bizResourceApply.getResourceId());
        if (StringUtils.isNull(resource))
        {
            throw new ServiceException("申请的资源不存在");
        }
        // 校验资源状态是否为可用
        if (!"0".equals(resource.getStatus()))
        {
            throw new ServiceException("申请的资源当前不可用");
        }
        // 校验申请数量是否超过可用数量
        if (bizResourceApply.getApplyCount() > resource.getAvailableCount())
        {
            throw new ServiceException("申请数量超过资源可用数量" + resource.getAvailableCount());
        }
        // 校验使用时间区间是否合理
        if (StringUtils.isNotNull(bizResourceApply.getBeginTime()) && StringUtils.isNotNull(bizResourceApply.getEndTime())
                && bizResourceApply.getEndTime().before(bizResourceApply.getBeginTime()))
        {
            throw new ServiceException("使用结束时间不能早于开始时间");
        }
        // 生成申请单编号，格式：BC + yyyyMMddHHmmss + 4位随机数
        bizResourceApply.setApplyNo(generateApplyNo());
        // 冗余资源名称与类型，便于列表展示
        bizResourceApply.setResourceName(resource.getResourceName());
        bizResourceApply.setResourceType(resource.getResourceType());
        // 填充申请人信息
        bizResourceApply.setApplyUserId(SecurityUtils.getUserId());
        bizResourceApply.setApplyUserName(SecurityUtils.getUsername());
        // 从登录用户信息中获取所属部门ID与部门名称
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotNull(loginUser.getSysUser()))
        {
            SysUser sysUser = loginUser.getSysUser();
            bizResourceApply.setApplyDeptId(sysUser.getDeptId());
            // 部门对象可能为空，需做非空判断
            if (StringUtils.isNotNull(sysUser.getDept()))
            {
                bizResourceApply.setApplyDeptName(sysUser.getDept().getDeptName());
            }
        }
        // 初始状态为待审批
        bizResourceApply.setStatus(STATUS_PENDING);
        bizResourceApply.setCreateBy(SecurityUtils.getUsername());
        bizResourceApply.setCreateTime(DateUtils.getNowDate());
        return bizResourceApplyMapper.insertBizResourceApply(bizResourceApply);
    }

    /**
     * 修改资源申请信息
     *
     * @param bizResourceApply 资源申请信息
     * @return 结果
     */
    @Override
    public int updateBizResourceApply(BizResourceApply bizResourceApply)
    {
        bizResourceApply.setUpdateTime(DateUtils.getNowDate());
        return bizResourceApplyMapper.updateBizResourceApply(bizResourceApply);
    }

    /**
     * 批量删除资源申请信息
     *
     * @param applyIds 需要删除的申请单ID
     * @return 结果
     */
    @Override
    public int deleteBizResourceApplyByIds(Long[] applyIds)
    {
        return bizResourceApplyMapper.deleteBizResourceApplyByIds(applyIds);
    }

    /**
     * 取消申请（仅待审批状态可取消）
     *
     * @param applyId 申请单ID
     * @return 结果
     */
    @Override
    public int cancelApply(Long applyId)
    {
        BizResourceApply apply = bizResourceApplyMapper.selectBizResourceApplyById(applyId);
        if (StringUtils.isNull(apply))
        {
            throw new ServiceException("申请单不存在");
        }
        // 只有待审批状态才允许取消
        if (!STATUS_PENDING.equals(apply.getStatus()))
        {
            throw new ServiceException("当前状态不允许取消");
        }
        // 非本人申请不可取消（管理员拥有 bocompute:audit:* 权限时放行，这里统一按本人校验）
        if (!SecurityUtils.getUserId().equals(apply.getApplyUserId()))
        {
            throw new ServiceException("只能取消本人提交的申请单");
        }
        BizResourceApply update = new BizResourceApply();
        update.setApplyId(applyId);
        update.setStatus(STATUS_CANCEL);
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        return bizResourceApplyMapper.updateBizResourceApply(update);
    }

    /**
     * 审批通过（占用资源并生成分配记录）
     *
     * @param applyId 申请单ID
     * @param auditOpinion 审批意见
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int passApply(Long applyId, String auditOpinion)
    {
        BizResourceApply apply = bizResourceApplyMapper.selectBizResourceApplyById(applyId);
        if (StringUtils.isNull(apply))
        {
            throw new ServiceException("申请单不存在");
        }
        // 只有待审批状态才允许审批
        if (!STATUS_PENDING.equals(apply.getStatus()))
        {
            throw new ServiceException("该申请单已审批，请勿重复操作");
        }
        // 1、占用资源数量（SQL 中带 available_count >= count 条件，防止并发超卖）
        int rows = bizResourceMapper.occupyResource(apply.getResourceId(), apply.getApplyCount());
        if (rows == 0)
        {
            throw new ServiceException("资源可用数量不足，审批失败");
        }
        // 2、更新申请单状态为已通过
        BizResourceApply update = new BizResourceApply();
        update.setApplyId(applyId);
        update.setStatus(STATUS_PASS);
        update.setAuditBy(SecurityUtils.getUsername());
        update.setAuditTime(DateUtils.getNowDate());
        update.setAuditOpinion(auditOpinion);
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        bizResourceApplyMapper.updateBizResourceApply(update);
        // 3、生成资源分配记录
        BizResourceAlloc alloc = new BizResourceAlloc();
        alloc.setApplyId(applyId);
        alloc.setApplyNo(apply.getApplyNo());
        alloc.setResourceId(apply.getResourceId());
        alloc.setResourceName(apply.getResourceName());
        alloc.setAllocCount(apply.getApplyCount());
        alloc.setUserId(apply.getApplyUserId());
        alloc.setUserName(apply.getApplyUserName());
        alloc.setDeptId(apply.getApplyDeptId());
        alloc.setDeptName(apply.getApplyDeptName());
        alloc.setBeginTime(apply.getBeginTime());
        alloc.setEndTime(apply.getEndTime());
        // 分配记录初始为使用中
        alloc.setStatus("0");
        alloc.setCreateBy(SecurityUtils.getUsername());
        alloc.setCreateTime(DateUtils.getNowDate());
        return bizResourceAllocMapper.insertBizResourceAlloc(alloc);
    }

    /**
     * 审批驳回
     *
     * @param applyId 申请单ID
     * @param auditOpinion 审批意见
     * @return 结果
     */
    @Override
    public int rejectApply(Long applyId, String auditOpinion)
    {
        BizResourceApply apply = bizResourceApplyMapper.selectBizResourceApplyById(applyId);
        if (StringUtils.isNull(apply))
        {
            throw new ServiceException("申请单不存在");
        }
        // 只有待审批状态才允许审批
        if (!STATUS_PENDING.equals(apply.getStatus()))
        {
            throw new ServiceException("该申请单已审批，请勿重复操作");
        }
        // 驳回原因必填
        if (StringUtils.isEmpty(auditOpinion))
        {
            throw new ServiceException("驳回时必须填写审批意见");
        }
        BizResourceApply update = new BizResourceApply();
        update.setApplyId(applyId);
        update.setStatus(STATUS_REJECT);
        update.setAuditBy(SecurityUtils.getUsername());
        update.setAuditTime(DateUtils.getNowDate());
        update.setAuditOpinion(auditOpinion);
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        return bizResourceApplyMapper.updateBizResourceApply(update);
    }

    /**
     * 查询待审批申请单数量
     *
     * @return 待审批数量
     */
    @Override
    public int countPendingApply()
    {
        return bizResourceApplyMapper.countPendingApply();
    }

    /**
     * 查询本月申请单数量
     *
     * @return 本月申请单数量
     */
    @Override
    public int countCurrentMonthApply()
    {
        return bizResourceApplyMapper.countCurrentMonthApply();
    }

    /**
     * 生成申请单编号，格式：BC + yyyyMMddHHmmss + 4位随机数
     *
     * @return 申请单编号
     */
    private String generateApplyNo()
    {
        // 时间戳部分
        String timePart = DateUtils.parseDateToStr("yyyyMMddHHmmss", new Date());
        // 4位随机数部分，保证同一秒内并发提交不重复
        int random = (int) ((Math.random() * 9 + 1) * 1000);
        return "BC" + timePart + random;
    }
}
