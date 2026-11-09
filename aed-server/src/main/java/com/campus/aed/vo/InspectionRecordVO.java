package com.campus.aed.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 巡检记录 VO(附带设备编号/名称与巡检员姓名)
 */
@Data
public class InspectionRecordVO {

    /** 记录ID */
    private Long id;

    /** 关联任务ID */
    private Long taskId;

    /** 设备ID */
    private Long deviceId;

    /** 设备编号 */
    private String deviceCode;

    /** 设备名称(型号) */
    private String deviceName;

    /** 巡检人ID */
    private Long inspectorId;

    /** 巡检人姓名 */
    private String inspectorName;

    /** 巡检时间 */
    private LocalDateTime inspectTime;

    /** 巡检结果: 1-正常 2-异常 */
    private Integer result;

    /** 总体说明 */
    private String summary;
}
