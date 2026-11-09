package com.campus.aed.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 巡检任务 VO(附带设备编号/名称与巡检员姓名)
 */
@Data
public class InspectionTaskVO {

    /** 任务ID */
    private Long id;

    /** 设备ID */
    private Long deviceId;

    /** 设备编号 */
    private String deviceCode;

    /** 设备名称(型号) */
    private String deviceName;

    /** 巡检人员ID */
    private Long inspectorId;

    /** 巡检人员姓名 */
    private String inspectorName;

    /** 计划巡检日期 */
    private LocalDate planDate;

    /** 状态: 0-待巡检 1-已完成 2-已逾期 3-已取消 */
    private Integer status;

    /** 巡检结果: 1-正常 2-异常 */
    private Integer result;

    /** 完成时间 */
    private LocalDateTime completeTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
