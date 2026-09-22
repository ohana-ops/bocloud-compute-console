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
import com.ruoyi.bocompute.domain.BizResource;
import com.ruoyi.bocompute.service.IBizResourceService;

/**
 * 算力资源操作处理
 * 
 * @author bocloud
 */
@RestController
@RequestMapping("/resource")
public class BizResourceController extends BaseController
{
    @Autowired
    private IBizResourceService bizResourceService;

    /**
     * 获取算力资源列表
     *
     * @param bizResource 查询条件
     * @return 分页后的资源列表
     */
    @RequiresPermissions("bocompute:resource:list")
    @GetMapping("/list")
    public TableDataInfo list(BizResource bizResource)
    {
        startPage();
        List<BizResource> list = bizResourceService.selectBizResourceList(bizResource);
        return getDataTable(list);
    }

    /**
     * 导出算力资源列表
     *
     * @param response HTTP响应
     * @param bizResource 查询条件
     */
    @RequiresPermissions("bocompute:resource:export")
    @Log(title = "算力资源", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizResource bizResource)
    {
        List<BizResource> list = bizResourceService.selectBizResourceList(bizResource);
        ExcelUtil<BizResource> util = new ExcelUtil<BizResource>(BizResource.class);
        util.exportExcel(response, list, "算力资源数据");
    }

    /**
     * 获取算力资源详细信息
     *
     * @param resourceId 资源ID
     * @return 资源详细信息
     */
    @RequiresPermissions("bocompute:resource:query")
    @GetMapping(value = "/{resourceId}")
    public AjaxResult getInfo(@PathVariable Long resourceId)
    {
        return success(bizResourceService.selectBizResourceById(resourceId));
    }

    /**
     * 获取所有可用算力资源（下拉选择用）
     *
     * @return 可用资源列表
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        return success(bizResourceService.selectBizResourceAll());
    }

    /**
     * 新增算力资源
     *
     * @param bizResource 资源信息
     * @return 结果
     */
    @RequiresPermissions("bocompute:resource:add")
    @Log(title = "算力资源", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BizResource bizResource)
    {
        // 校验资源编码是否唯一
        if (!bizResourceService.checkResourceCodeUnique(bizResource))
        {
            return error("新增资源'" + bizResource.getResourceName() + "'失败，资源编码已存在");
        }
        bizResource.setCreateBy(SecurityUtils.getUsername());
        return toAjax(bizResourceService.insertBizResource(bizResource));
    }

    /**
     * 修改算力资源
     *
     * @param bizResource 资源信息
     * @return 结果
     */
    @RequiresPermissions("bocompute:resource:edit")
    @Log(title = "算力资源", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BizResource bizResource)
    {
        // 校验资源编码是否唯一
        if (!bizResourceService.checkResourceCodeUnique(bizResource))
        {
            return error("修改资源'" + bizResource.getResourceName() + "'失败，资源编码已存在");
        }
        bizResource.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(bizResourceService.updateBizResource(bizResource));
    }

    /**
     * 删除算力资源
     *
     * @param resourceIds 资源ID数组
     * @return 结果
     */
    @RequiresPermissions("bocompute:resource:remove")
    @Log(title = "算力资源", businessType = BusinessType.DELETE)
    @DeleteMapping("/{resourceIds}")
    public AjaxResult remove(@PathVariable Long[] resourceIds)
    {
        return toAjax(bizResourceService.deleteBizResourceByIds(resourceIds));
    }
}
