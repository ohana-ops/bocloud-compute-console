package com.ruoyi.bocompute.controller;

import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.ruoyi.bocompute.domain.BizAllocDevice;
import com.ruoyi.bocompute.domain.BizDevice;
import com.ruoyi.bocompute.service.IBizDeviceService;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;

/**
 * 模拟算力设备操作处理
 *
 * @author bocloud
 */
@RestController
@RequestMapping("/device")
public class BizDeviceController extends BaseController
{
    @Autowired
    private IBizDeviceService bizDeviceService;

    /**
     * 获取模拟算力设备列表
     *
     * @param bizDevice 查询条件
     * @return 分页后的设备列表
     */
    @RequiresPermissions("bocompute:device:list")
    @GetMapping("/list")
    public TableDataInfo list(BizDevice bizDevice)
    {
        Page<BizDevice> page = getPage();
        IPage<BizDevice> result = bizDeviceService.selectBizDeviceList(page, bizDevice);
        return getDataTable(result);
    }

    /**
     * 导出模拟算力设备列表
     *
     * @param response HTTP响应
     * @param bizDevice 查询条件
     */
    @RequiresPermissions("bocompute:device:export")
    @Log(title = "算力设备", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizDevice bizDevice)
    {
        List<BizDevice> list = bizDeviceService.selectBizDeviceList(new Page<BizDevice>(1, -1), bizDevice).getRecords();
        ExcelUtil<BizDevice> util = new ExcelUtil<BizDevice>(BizDevice.class);
        util.exportExcel(response, list, "算力设备数据");
    }

    /**
     * 获取模拟算力设备详细信息
     *
     * @param deviceId 设备ID
     * @return 设备详细信息
     */
    @RequiresPermissions("bocompute:device:query")
    @GetMapping(value = "/{deviceId}")
    public AjaxResult getInfo(@PathVariable Long deviceId)
    {
        return success(bizDeviceService.selectBizDeviceById(deviceId));
    }

    /**
     * 查询某次分配绑定的设备清单
     *
     * @param allocId 分配单ID
     * @return 设备绑定清单
     */
    @RequiresPermissions("bocompute:device:query")
    @GetMapping(value = "/byAlloc/{allocId}")
    public AjaxResult listByAlloc(@PathVariable Long allocId)
    {
        List<BizAllocDevice> list = bizDeviceService.selectDevicesByAllocId(allocId);
        return success(list);
    }

    /**
     * 新增模拟算力设备
     *
     * @param bizDevice 设备信息
     * @return 结果
     */
    @RequiresPermissions("bocompute:device:add")
    @Log(title = "算力设备", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BizDevice bizDevice)
    {
        // 校验设备编号是否唯一
        if (!bizDeviceService.checkDeviceCodeUnique(bizDevice))
        {
            return error("新增设备'" + bizDevice.getDeviceCode() + "'失败，设备编号已存在");
        }
        bizDevice.setCreateBy(SecurityUtils.getUsername());
        return toAjax(bizDeviceService.insertBizDevice(bizDevice));
    }

    /**
     * 按资源池批量初始化设备
     *
     * @param params 初始化参数：resourceId、nodePrefix、nodeCount、cardsPerNode、confirm
     * @return 结果
     */
    @RequiresPermissions("bocompute:device:add")
    @Log(title = "算力设备", businessType = BusinessType.INSERT)
    @PostMapping("/batchInit")
    public AjaxResult batchInit(@RequestBody java.util.Map<String, Object> params)
    {
        // 资源池ID
        Long resourceId = toLong(params.get("resourceId"));
        // 节点名前缀
        String nodePrefix = params.get("nodePrefix") == null ? "" : String.valueOf(params.get("nodePrefix"));
        // 节点数量
        Integer nodeCount = toInt(params.get("nodeCount"));
        // 每节点卡数
        Integer cardsPerNode = toInt(params.get("cardsPerNode"));
        // 池内已有设备时是否确认追加
        boolean confirm = Boolean.TRUE.equals(params.get("confirm"));
        return success(bizDeviceService.batchInitDevices(resourceId, nodePrefix, nodeCount, cardsPerNode, confirm));
    }

    /**
     * 修改模拟算力设备
     *
     * @param bizDevice 设备信息
     * @return 结果
     */
    @RequiresPermissions("bocompute:device:edit")
    @Log(title = "算力设备", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BizDevice bizDevice)
    {
        return toAjax(bizDeviceService.updateBizDevice(bizDevice));
    }

    /**
     * 设备设为故障（仅空闲设备可操作）
     *
     * @param deviceId 设备ID
     * @return 结果
     */
    @RequiresPermissions("bocompute:device:fault")
    @Log(title = "算力设备", businessType = BusinessType.UPDATE)
    @PutMapping("/fault/{deviceId}")
    public AjaxResult fault(@PathVariable Long deviceId)
    {
        return toAjax(bizDeviceService.changeDeviceStatus(deviceId, BizDevice.STATUS_FAULT));
    }

    /**
     * 设备设为维护（仅空闲设备可操作）
     *
     * @param deviceId 设备ID
     * @return 结果
     */
    @RequiresPermissions("bocompute:device:edit")
    @Log(title = "算力设备", businessType = BusinessType.UPDATE)
    @PutMapping("/maintain/{deviceId}")
    public AjaxResult maintain(@PathVariable Long deviceId)
    {
        return toAjax(bizDeviceService.changeDeviceStatus(deviceId, BizDevice.STATUS_MAINTAIN));
    }

    /**
     * 设备上线（故障 / 维护 → 空闲）
     *
     * @param deviceId 设备ID
     * @return 结果
     */
    @RequiresPermissions("bocompute:device:online")
    @Log(title = "算力设备", businessType = BusinessType.UPDATE)
    @PutMapping("/online/{deviceId}")
    public AjaxResult online(@PathVariable Long deviceId)
    {
        return toAjax(bizDeviceService.changeDeviceStatus(deviceId, BizDevice.STATUS_IDLE));
    }

    /**
     * 删除模拟算力设备（已分配设备不允许删除）
     *
     * @param deviceIds 设备ID数组
     * @return 结果
     */
    @RequiresPermissions("bocompute:device:remove")
    @Log(title = "算力设备", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deviceIds}")
    public AjaxResult remove(@PathVariable Long[] deviceIds)
    {
        return toAjax(bizDeviceService.deleteBizDeviceByIds(deviceIds));
    }

    /**
     * 把请求参数转换为 Long
     *
     * @param value 参数值
     * @return Long 值，转换失败返回 null
     */
    private Long toLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    /**
     * 把请求参数转换为 Integer
     *
     * @param value 参数值
     * @return Integer 值，转换失败返回 null
     */
    private Integer toInt(Object value)
    {
        if (value == null)
        {
            return null;
        }
        return Integer.valueOf(String.valueOf(value));
    }
}
