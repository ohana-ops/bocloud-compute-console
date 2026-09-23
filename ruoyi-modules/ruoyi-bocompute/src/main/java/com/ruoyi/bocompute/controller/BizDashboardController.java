package com.ruoyi.bocompute.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.bocompute.domain.BizResource;
import com.ruoyi.bocompute.service.IBizDeviceService;
import com.ruoyi.bocompute.service.IBizResourceApplyService;
import com.ruoyi.bocompute.service.IBizResourceService;

/**
 * 使用统计看板操作处理
 * 
 * @author bocloud
 */
@RestController
@RequestMapping("/dashboard")
public class BizDashboardController extends BaseController
{
    @Autowired
    private IBizResourceService bizResourceService;

    @Autowired
    private IBizResourceApplyService bizResourceApplyService;

    @Autowired
    private IBizDeviceService bizDeviceService;

    /**
     * 获取看板统计数据
     * 包含：资源总数、已分配总数、可用总数、待审批申请数、本月申请数、设备状态分布
     *
     * @return 统计结果
     */
    @GetMapping("/statistics")
    public AjaxResult statistics()
    {
        // 查询全部资源，用于汇总总数
        List<BizResource> resourceList = bizResourceService.selectBizResourceList(new Page<BizResource>(1, -1), new BizResource()).getRecords();
        // 资源总数量累加
        int totalCount = 0;
        // 已分配数量累加
        int usedCount = 0;
        // 可用数量累加
        int availableCount = 0;
        for (BizResource resource : resourceList)
        {
            totalCount += resource.getTotalCount() == null ? 0 : resource.getTotalCount();
            usedCount += resource.getUsedCount() == null ? 0 : resource.getUsedCount();
            availableCount += resource.getAvailableCount() == null ? 0 : resource.getAvailableCount();
        }
        Map<String, Object> data = new HashMap<String, Object>();
        // 资源总数量
        data.put("totalCount", totalCount);
        // 已分配数量
        data.put("usedCount", usedCount);
        // 可用数量
        data.put("availableCount", availableCount);
        // 资源利用率（已分配 / 总数量，保留两位小数）
        double usageRate = totalCount == 0 ? 0.0 : (double) usedCount / totalCount * 100;
        data.put("usageRate", Math.round(usageRate * 100) / 100.0);
        // 资源种类数量
        data.put("resourceKinds", resourceList.size());
        // 待审批申请单数量
        data.put("pendingApplyCount", bizResourceApplyService.countPendingApply());
        // 本月申请单数量
        data.put("monthApplyCount", bizResourceApplyService.countCurrentMonthApply());
        // 各资源占用明细，用于图表展示
        data.put("resources", resourceList);
        // 设备维度统计：设备总数、空闲、已分配、故障、维护
        data.put("devices", bizDeviceService.selectDeviceStatusStatistics());
        return success(data);
    }
}
