package com.ruoyi.bocompute.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.bocompute.domain.BizAllocDevice;
import com.ruoyi.bocompute.domain.BizDevice;
import com.ruoyi.bocompute.domain.BizResource;
import com.ruoyi.bocompute.domain.BizResourceAlloc;
import com.ruoyi.bocompute.domain.BizResourceApply;
import com.ruoyi.bocompute.mapper.BizAllocDeviceMapper;
import com.ruoyi.bocompute.mapper.BizResourceAllocMapper;
import com.ruoyi.bocompute.mapper.BizResourceApplyMapper;
import com.ruoyi.bocompute.mapper.BizResourceMapper;
import com.ruoyi.bocompute.schedule.ComputeScheduler;
import com.ruoyi.bocompute.schedule.DeviceCodeGenerator;
import com.ruoyi.bocompute.schedule.ScheduleRequest;
import com.ruoyi.bocompute.schedule.ScheduleResult;
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

    @Autowired
    private BizAllocDeviceMapper bizAllocDeviceMapper;

    /**
     * 调度器实现，由 bocompute.scheduler.type 配置装配（device-pool / k8s）
     */
    @Autowired(required = false)
    private ComputeScheduler computeScheduler;

    /**
     * 分页查询资源申请信息集合
     * 
     * @param page 分页参数
     * @param bizResourceApply 资源申请信息
     * @return 资源申请信息集合
     */
    @Override
    public IPage<BizResourceApply> selectBizResourceApplyList(IPage<BizResourceApply> page, BizResourceApply bizResourceApply)
    {
        return bizResourceApplyMapper.selectBizResourceApplyList(page, bizResourceApply);
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
        BizResourceApply apply = bizResourceApplyMapper.selectBizResourceApplyById(applyId);
        // 已通过的申请单，把分配记录上的设备编号组装回来，避免前端连打三个接口
        if (StringUtils.isNotNull(apply) && STATUS_PASS.equals(apply.getStatus()))
        {
            BizResourceAlloc alloc = bizResourceAllocMapper.selectBizResourceAllocByApplyId(applyId);
            if (StringUtils.isNotNull(alloc))
            {
                apply.setDeviceCodes(alloc.getDeviceCodes());
                apply.setNodeNames(alloc.getNodeNames());
            }
        }
        return apply;
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
     * 审批通过（插分配单 → 调度选卡锁卡 → 回写卡号 → 同步库存 → 单据通过）
     *
     * 全流程在同一事务内完成，任一步失败整单回滚，申请单保持待审批状态。
     * 顺序上必须「先插分配单拿到 allocId，再调度锁设备」，禁止先把申请单改为已通过。
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
        // 1、校验资源存在且可用
        BizResource resource = bizResourceMapper.selectBizResourceById(apply.getResourceId());
        if (StringUtils.isNull(resource))
        {
            throw new ServiceException("申请的资源不存在");
        }
        if (!"0".equals(resource.getStatus()))
        {
            throw new ServiceException("申请的资源当前不可用，无法审批通过");
        }
        // 校验调度器实现是否已装配
        if (StringUtils.isNull(computeScheduler))
        {
            throw new ServiceException("未找到可用的调度器实现，请检查 bocompute.scheduler.type 配置");
        }
        // 2、先插入分配单，拿到 allocId 后交给调度器锁设备
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
        bizResourceAllocMapper.insertBizResourceAlloc(alloc);
        // 3、调度选卡并锁定设备（空闲设备不足、并发抢占、K8s 创建 Pod 失败都会在这里抛异常回滚）
        ScheduleRequest request = new ScheduleRequest();
        request.setApplyId(applyId);
        request.setApplyNo(apply.getApplyNo());
        request.setResourceId(apply.getResourceId());
        request.setResourceType(apply.getResourceType());
        request.setCount(apply.getApplyCount());
        request.setUserId(apply.getApplyUserId());
        request.setUserName(apply.getApplyUserName());
        request.setBeginTime(apply.getBeginTime());
        request.setEndTime(apply.getEndTime());
        request.setAllocId(alloc.getAllocId());
        ScheduleResult scheduleResult = computeScheduler.allocate(request);
        // 4、写入分配单-设备绑定明细
        saveAllocDevices(alloc, apply, scheduleResult.getDevices());
        // 5、回写分配单的调度结果（卡号、节点、调度说明）
        BizResourceAlloc allocUpdate = new BizResourceAlloc();
        allocUpdate.setAllocId(alloc.getAllocId());
        allocUpdate.setDeviceCodes(scheduleResult.getDeviceCodes());
        allocUpdate.setNodeNames(scheduleResult.getNodeNames());
        allocUpdate.setScheduleType(scheduleResult.getScheduleType());
        allocUpdate.setScheduleMsg(scheduleResult.getMessage());
        allocUpdate.setUpdateBy(SecurityUtils.getUsername());
        bizResourceAllocMapper.updateBizResourceAlloc(allocUpdate);
        // 6、回写资源池库存：设备类以设备状态为准，cpu 仍按数量扣减
        syncResourceInventory(apply.getResourceType(), apply.getResourceId(), apply.getApplyCount());
        // 7、申请单改为已通过，放在最后一步，保证调度失败时单据状态不变
        BizResourceApply update = new BizResourceApply();
        update.setApplyId(applyId);
        update.setStatus(STATUS_PASS);
        update.setAuditBy(SecurityUtils.getUsername());
        update.setAuditTime(DateUtils.getNowDate());
        update.setAuditOpinion(auditOpinion);
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        return bizResourceApplyMapper.updateBizResourceApply(update);
    }

    /**
     * 保存分配单-设备绑定明细
     *
     * @param alloc 分配单
     * @param apply 申请单
     * @param devices 调度选中的设备列表，cpu 类型为空
     */
    private void saveAllocDevices(BizResourceAlloc alloc, BizResourceApply apply, List<BizDevice> devices)
    {
        // cpu 等无设备场景不写绑定明细
        if (StringUtils.isNull(devices) || devices.isEmpty())
        {
            return;
        }
        List<BizAllocDevice> list = new ArrayList<BizAllocDevice>();
        Date now = DateUtils.getNowDate();
        for (BizDevice device : devices)
        {
            BizAllocDevice allocDevice = new BizAllocDevice();
            allocDevice.setAllocId(alloc.getAllocId());
            allocDevice.setApplyId(apply.getApplyId());
            allocDevice.setDeviceId(device.getDeviceId());
            allocDevice.setDeviceCode(device.getDeviceCode());
            allocDevice.setNodeName(device.getNodeName());
            allocDevice.setResourceId(device.getResourceId());
            allocDevice.setBindTime(now);
            // 绑定记录初始为占用中
            allocDevice.setStatus(BizAllocDevice.STATUS_USING);
            list.add(allocDevice);
        }
        bizAllocDeviceMapper.batchInsertBizAllocDevice(list);
    }

    /**
     * 回写资源池库存
     * gpu/npu/node：设备是真相源，按设备状态汇总回写；
     * cpu：没有设备，沿用数量扣减（SQL 带 available_count >= count 防超卖）。
     *
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param count 本次操作数量
     */
    private void syncResourceInventory(String resourceType, Long resourceId, Integer count)
    {
        if (DeviceCodeGenerator.TYPE_CPU.equals(resourceType))
        {
            int rows = bizResourceMapper.occupyResource(resourceId, count);
            if (rows == 0)
            {
                throw new ServiceException("资源可用数量不足，审批失败");
            }
            return;
        }
        bizResourceMapper.syncResourceCount(resourceId);
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
