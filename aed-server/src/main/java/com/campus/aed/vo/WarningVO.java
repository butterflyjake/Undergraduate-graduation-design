package com.campus.aed.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预警信息视图对象(预警记录 + 设备信息 + 配件信息)
 */
@Data
@Schema(description = "预警信息视图对象")
public class WarningVO {

    /** 预警ID */
    @Schema(description = "预警ID")
    private Long id;

    /** 配件ID */
    @Schema(description = "配件ID")
    private Long partId;

    /** 设备ID */
    @Schema(description = "设备ID")
    private Long deviceId;

    /** 设备编号, 如 AED-001 */
    @Schema(description = "设备编号")
    private String deviceCode;

    /** 设备名称(型号) */
    @Schema(description = "设备名称")
    private String deviceName;

    /** 配件类型: 1-电极片 2-电池 */
    @Schema(description = "配件类型: 1-电极片 2-电池")
    private Integer partType;

    /** 配件有效期至 */
    @Schema(description = "配件有效期至")
    private LocalDate expireDate;

    /** 预警类型: 1-临期(30天内到期) 2-已过期 */
    @Schema(description = "预警类型: 1-临期(30天内到期) 2-已过期")
    private Integer warningType;

    /** 剩余天数(已过期为负数) */
    @Schema(description = "剩余天数(已过期为负数)")
    private Integer daysRemaining;

    /** 预警生成日期 */
    @Schema(description = "预警生成日期")
    private LocalDate warningDate;

    /** 状态: 0-未处理 1-已处理 */
    @Schema(description = "状态: 0-未处理 1-已处理")
    private Integer status;

    /** 处理说明 */
    @Schema(description = "处理说明")
    private String handleDesc;

    /** 处理时间 */
    @Schema(description = "处理时间")
    private LocalDateTime handleTime;
}
