package com.campus.aed.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.aed.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 巡检任务实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inspection_task")
public class InspectionTask extends BaseEntity {

    /** 设备ID */
    private Long deviceId;

    /** 巡检人员ID */
    private Long inspectorId;

    /** 计划巡检日期 */
    private LocalDate planDate;

    /** 状态: 0-待巡检 1-已完成 2-已逾期 3-已取消 */
    private Integer status;

    /** 巡检结果: 1-正常 2-异常 */
    private Integer result;

    /** 完成时间 */
    private LocalDateTime completeTime;
}
