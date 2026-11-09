package com.campus.aed.task;

import com.campus.aed.service.ExpiryWarningService;
import com.campus.aed.util.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 配件到期预警扫描定时任务(每天 07:00)
 * 扫描使用中配件的有效期, 生成临期/过期预警
 */
@Slf4j
@DisallowConcurrentExecution
public class ExpiryWarningJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // 锁住整个扫描事务(提交完成后才释放), 防止并发扫描时"先查后插"去重失效、重复生成预警
        synchronized (ExpiryWarningJob.class) {
            try {
                ExpiryWarningService service = SpringContextHolder.getBean(ExpiryWarningService.class);
                service.scanExpiry();
                log.info("配件到期预警扫描完成");
            } catch (Exception e) {
                log.error("配件到期预警扫描失败", e);
            }
        }
    }
}
