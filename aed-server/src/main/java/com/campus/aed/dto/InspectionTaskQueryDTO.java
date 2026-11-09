package com.campus.aed.dto;

import com.campus.aed.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 巡检任务分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InspectionTaskQueryDTO extends PageQuery {

    /** 任务状态: 0-待巡检 1-已完成 2-已逾期 3-已取消(可选过滤条件) */
    private Integer status;

    /** 设备ID(可选过滤条件) */
    private Long deviceId;

    /** 计划巡检日期(可选过滤条件) */
    private LocalDate planDate;
}
