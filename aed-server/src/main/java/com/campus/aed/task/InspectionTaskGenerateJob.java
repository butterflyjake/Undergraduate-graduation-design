package com.campus.aed.task;

import com.campus.aed.service.InspectionTaskService;
import com.campus.aed.util.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDate;

/**
 * 巡检任务自动生成定时任务(每天 08:00)
 * 为所有"在用"设备生成当日巡检任务, 并将昨日未完成任务标记为已逾期
 */
@Slf4j
public class InspectionTaskGenerateJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            InspectionTaskService service = SpringContextHolder.getBean(InspectionTaskService.class);
            service.generateTasks(LocalDate.now());
            log.info("巡检任务自动生成完成: {}", LocalDate.now());
        } catch (Exception e) {
            log.error("巡检任务自动生成失败", e);
        }
    }
}
