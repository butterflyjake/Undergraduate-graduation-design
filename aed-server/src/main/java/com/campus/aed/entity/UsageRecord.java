package com.campus.aed.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.aed.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AED使用记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("usage_record")
public class UsageRecord extends BaseEntity {

    /** 设备ID */
    private Long deviceId;

    /** 使用发生时间 */
    private LocalDateTime occurTime;

    /** 事发地点描述 */
    private String locationDesc;

    /** 患者情况(匿名描述) */
    private String patientDesc;

    /** 处置结果 */
    private String resultDesc;

    /** 登记人ID */
    private Long recorderId;

    /** 备注 */
    private String remark;
}
