package com.ruoyi.bocompute.controller;

import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import com.ruoyi.bocompute.domain.BizResourceAlloc;
import com.ruoyi.bocompute.service.IBizResourceAllocService;

/**
 * 资源分配记录操作处理
 * 
 * @author bocloud
 */
@RestController
@RequestMapping("/alloc")
public class BizResourceAllocController extends BaseController
{
    @Autowired
    private IBizResourceAllocService bizResourceAllocService;

    /**
     * 获取资源分配记录列表
     *
     * @param bizResourceAlloc 查询条件
     * @return 分页后的分配记录列表
     */
    @RequiresPermissions("bocompute:alloc:list")
    @GetMapping("/list")
    public TableDataInfo list(BizResourceAlloc bizResourceAlloc)
    {
        Page<BizResourceAlloc> page = getPage();
        IPage<BizResourceAlloc> result = bizResourceAllocService.selectBizResourceAllocList(page, bizResourceAlloc);
        return getDataTable(result);
    }

    /**
     * 获取我的资源列表（仅查询当前登录人已分配的资源）
     *
     * @param bizResourceAlloc 查询条件
     * @return 分页后的分配记录列表
     */
    @GetMapping("/myList")
    public TableDataInfo myList(BizResourceAlloc bizResourceAlloc)
    {
        Page<BizResourceAlloc> page = getPage();
        // 限定只查询当前登录人自己的分配记录
        bizResourceAlloc.setUserId(SecurityUtils.getUserId());
        IPage<BizResourceAlloc> result = bizResourceAllocService.selectBizResourceAllocList(page, bizResourceAlloc);
        return getDataTable(result);
    }

    /**
     * 导出资源分配记录
     *
     * @param response HTTP响应
     * @param bizResourceAlloc 查询条件
     */
    @RequiresPermissions("bocompute:alloc:export")
    @Log(title = "资源分配", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizResourceAlloc bizResourceAlloc)
    {
        List<BizResourceAlloc> list = bizResourceAllocService.selectBizResourceAllocList(new Page<BizResourceAlloc>(1, -1), bizResourceAlloc).getRecords();
        ExcelUtil<BizResourceAlloc> util = new ExcelUtil<BizResourceAlloc>(BizResourceAlloc.class);
        util.exportExcel(response, list, "资源分配数据");
    }

    /**
     * 获取分配记录详细信息
     *
     * @param allocId 分配记录ID
     * @return 分配记录详细信息
     */
    @RequiresPermissions("bocompute:alloc:query")
    @GetMapping(value = "/{allocId}")
    public AjaxResult getInfo(@PathVariable Long allocId)
    {
        return success(bizResourceAllocService.selectBizResourceAllocById(allocId));
    }

    /**
     * 释放资源（回收已分配资源并回写资源可用数量）
     *
     * @param allocId 分配记录ID
     * @return 结果
     */
    @RequiresPermissions("bocompute:alloc:release")
    @Log(title = "资源分配", businessType = BusinessType.UPDATE)
    @PutMapping("/release/{allocId}")
    public AjaxResult release(@PathVariable Long allocId)
    {
        return toAjax(bizResourceAllocService.releaseAlloc(allocId));
    }

    /**
     * 删除分配记录
     *
     * @param allocIds 分配记录ID数组
     * @return 结果
     */
    @RequiresPermissions("bocompute:alloc:remove")
    @Log(title = "资源分配", businessType = BusinessType.DELETE)
    @DeleteMapping("/{allocIds}")
    public AjaxResult remove(@PathVariable Long[] allocIds)
    {
        return toAjax(bizResourceAllocService.deleteBizResourceAllocByIds(allocIds));
    }
}
