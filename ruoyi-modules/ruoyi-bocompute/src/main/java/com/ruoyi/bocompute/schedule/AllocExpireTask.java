package com.ruoyi.bocompute.schedule;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.bocompute.domain.BizResourceAlloc;
import com.ruoyi.bocompute.mapper.BizResourceAllocMapper;
import com.ruoyi.bocompute.service.IBizResourceAllocService;
import com.ruoyi.common.core.context.SecurityContextHolder;

/**
 * 分配单到期自动释放任务
 * 
 * 每分钟扫描一次「使用中且已过结束时间」的分配单，逐条调用已有的 releaseAlloc 完成还卡。
 * 单条失败只记录日志，不影响其他分配单，避免一条脏数据卡住整个回收流程。
 * 开关：bocompute.scheduler.auto-release-enabled（默认开启）。
 *
 * @author bocloud
 */
@Component
@ConditionalOnProperty(prefix = "bocompute.scheduler", name = "auto-release-enabled", havingValue = "true", matchIfMissing = true)
public class AllocExpireTask
{
    /** 定时任务执行人标识，便于在分配记录上区分自动回收与人工释放 */
    private static final String SYSTEM_OPERATOR = "system";

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(AllocExpireTask.class);

    @Autowired
    private BizResourceAllocMapper bizResourceAllocMapper;

    @Autowired
    private IBizResourceAllocService bizResourceAllocService;

    /**
     * 扫描到期分配单并自动释放（每分钟执行一次）
     */
    @Scheduled(cron = "0 * * * * ?")
    public void releaseExpiredAlloc()
    {
        List<BizResourceAlloc> allocList = bizResourceAllocMapper.selectExpireAllocList();
        if (allocList == null || allocList.isEmpty())
        {
            return;
        }
        log.info("到期自动释放任务开始，本次待处理分配单数量：{}", allocList.size());
        // 定时任务线程没有登录上下文，这里显式写入操作人，便于审计
        SecurityContextHolder.setUserName(SYSTEM_OPERATOR);
        try
        {
            for (BizResourceAlloc alloc : allocList)
            {
                try
                {
                    bizResourceAllocService.releaseAlloc(alloc.getAllocId());
                    log.info("分配单 {} 已到期自动释放，使用人：{}", alloc.getAllocId(), alloc.getUserName());
                }
                catch (Exception e)
                {
                    // 单条失败不影响其他分配单，仅记录日志，下一轮会再次扫描
                    log.error("分配单 " + alloc.getAllocId() + " 到期自动释放失败：" + e.getMessage(), e);
                }
            }
        }
        finally
        {
            // 清理线程变量，避免污染线程池中的后续任务
            SecurityContextHolder.setUserName("");
        }
    }
}
