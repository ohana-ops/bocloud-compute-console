package com.ruoyi.bocompute.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.bocompute.domain.BizResourceApply;
import com.ruoyi.bocompute.service.IBizResourceApplyService;

/**
 * 资源申请操作处理
 * 
 * @author bocloud
 */
@RestController
@RequestMapping("/apply")
public class BizResourceApplyController extends BaseController
{
    @Autowired
    private IBizResourceApplyService bizResourceApplyService;

    /**
     * 获取我的申请列表（仅查询当前登录人提交的申请单）
     *
     * @param bizResourceApply 查询条件
     * @return 分页后的申请单列表
     */
    @RequiresPermissions("bocompute:apply:list")
    @GetMapping("/myList")
    public TableDataInfo myList(BizResourceApply bizResourceApply)
    {
        startPage();
        // 限定只查询当前登录人自己的申请单
        bizResourceApply.setApplyUserId(SecurityUtils.getUserId());
        List<BizResourceApply> list = bizResourceApplyService.selectBizResourceApplyList(bizResourceApply);
        return getDataTable(list);
    }

    /**
     * 获取全部申请列表（审批管理员使用）
     *
     * @param bizResourceApply 查询条件
     * @return 分页后的申请单列表
     */
    @RequiresPermissions("bocompute:audit:list")
    @GetMapping("/list")
    public TableDataInfo list(BizResourceApply bizResourceApply)
    {
        startPage();
        List<BizResourceApply> list = bizResourceApplyService.selectBizResourceApplyList(bizResourceApply);
        return getDataTable(list);
    }

    /**
     * 导出申请单列表
     *
     * @param response HTTP响应
     * @param bizResourceApply 查询条件
     */
    @RequiresPermissions("bocompute:apply:export")
    @Log(title = "资源申请", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizResourceApply bizResourceApply)
    {
        List<BizResourceApply> list = bizResourceApplyService.selectBizResourceApplyList(bizResourceApply);
        ExcelUtil<BizResourceApply> util = new ExcelUtil<BizResourceApply>(BizResourceApply.class);
        util.exportExcel(response, list, "资源申请数据");
    }

    /**
     * 获取申请单详细信息
     *
     * @param applyId 申请单ID
     * @return 申请单详细信息
     */
    @RequiresPermissions("bocompute:apply:query")
    @GetMapping(value = "/{applyId}")
    public AjaxResult getInfo(@PathVariable Long applyId)
    {
        return success(bizResourceApplyService.selectBizResourceApplyById(applyId));
    }

    /**
     * 提交资源申请
     *
     * @param bizResourceApply 申请信息
     * @return 结果
     */
    @RequiresPermissions("bocompute:apply:add")
    @Log(title = "资源申请", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BizResourceApply bizResourceApply)
    {
        return toAjax(bizResourceApplyService.insertBizResourceApply(bizResourceApply));
    }

    /**
     * 修改资源申请
     *
     * @param bizResourceApply 申请信息
     * @return 结果
     */
    @RequiresPermissions("bocompute:apply:edit")
    @Log(title = "资源申请", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BizResourceApply bizResourceApply)
    {
        bizResourceApply.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(bizResourceApplyService.updateBizResourceApply(bizResourceApply));
    }

    /**
     * 取消申请（仅待审批状态可取消）
     *
     * @param applyId 申请单ID
     * @return 结果
     */
    @RequiresPermissions("bocompute:apply:cancel")
    @Log(title = "资源申请", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{applyId}")
    public AjaxResult cancel(@PathVariable Long applyId)
    {
        return toAjax(bizResourceApplyService.cancelApply(applyId));
    }

    /**
     * 删除申请单
     *
     * @param applyIds 申请单ID数组
     * @return 结果
     */
    @RequiresPermissions("bocompute:apply:remove")
    @Log(title = "资源申请", businessType = BusinessType.DELETE)
    @DeleteMapping("/{applyIds}")
    public AjaxResult remove(@PathVariable Long[] applyIds)
    {
        return toAjax(bizResourceApplyService.deleteBizResourceApplyByIds(applyIds));
    }

    /**
     * 审批通过
     *
     * @param bizResourceApply 审批参数（含申请单ID与审批意见）
     * @return 结果
     */
    @RequiresPermissions("bocompute:audit:pass")
    @Log(title = "资源审批", businessType = BusinessType.UPDATE)
    @PutMapping("/pass")
    public AjaxResult pass(@RequestBody BizResourceApply bizResourceApply)
    {
        return toAjax(bizResourceApplyService.passApply(bizResourceApply.getApplyId(), bizResourceApply.getAuditOpinion()));
    }

    /**
     * 审批驳回
     *
     * @param bizResourceApply 审批参数（含申请单ID与审批意见）
     * @return 结果
     */
    @RequiresPermissions("bocompute:audit:reject")
    @Log(title = "资源审批", businessType = BusinessType.UPDATE)
    @PutMapping("/reject")
    public AjaxResult reject(@RequestBody BizResourceApply bizResourceApply)
    {
        return toAjax(bizResourceApplyService.rejectApply(bizResourceApply.getApplyId(), bizResourceApply.getAuditOpinion()));
    }
}
