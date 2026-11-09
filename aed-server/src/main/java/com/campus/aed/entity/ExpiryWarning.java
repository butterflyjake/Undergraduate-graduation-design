package com.campus.aed.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.aed.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 配件到期预警实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("expiry_warning")
public class ExpiryWarning extends BaseEntity {

    /** 配件ID */
    private Long partId;

    /** 设备ID */
    private Long deviceId;

    /** 预警类型: 1-临期(30天内到期) 2-已过期 */
    private Integer warningType;

    /** 剩余天数(已过期为负数) */
    private Integer daysRemaining;

    /** 预警生成日期 */
    private LocalDate warningDate;

    /** 状态: 0-未处理 1-已处理 */
    private Integer status;

    /** 处理说明 */
    private String handleDesc;

    /** 处理时间 */
    private LocalDateTime handleTime;
}
