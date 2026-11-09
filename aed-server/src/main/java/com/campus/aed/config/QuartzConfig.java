package com.campus.aed.config;

import com.campus.aed.task.ExpiryWarningJob;
import com.campus.aed.task.InspectionTaskGenerateJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz 定时任务配置
 * Spring Boot 会自动将 JobDetail/Trigger Bean 注册到调度器中执行
 */
@Configuration
public class QuartzConfig {

    /** 巡检任务自动生成: 每天 08:00 执行 */
    @Bean
    public JobDetail inspectionTaskGenerateJobDetail() {
        return JobBuilder.newJob(InspectionTaskGenerateJob.class)
                .withIdentity("inspectionTaskGenerateJob", "aed")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger inspectionTaskGenerateTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(inspectionTaskGenerateJobDetail())
                .withIdentity("inspectionTaskGenerateTrigger", "aed")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 8 * * ?"))
                .build();
    }

    /** 配件到期预警扫描: 每天 07:00 执行 */
    @Bean
    public JobDetail expiryWarningJobDetail() {
        return JobBuilder.newJob(ExpiryWarningJob.class)
                .withIdentity("expiryWarningJob", "aed")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger expiryWarningTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(expiryWarningJobDetail())
                .withIdentity("expiryWarningTrigger", "aed")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 7 * * ?"))
                .build();
    }
}
