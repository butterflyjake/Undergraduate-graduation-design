package com.campus.aed.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AED使用记录 VO(含设备与登记人冗余信息, 关联数据缺失时为 null)
 */
@Data
public class UsageVO {

    /** 记录ID */
    private Long id;

    /** 设备ID */
    private Long deviceId;

    /** 设备编号(设备不存在时为 null) */
    private String deviceCode;

    /** 设备名称(设备不存在时为 null) */
    private String deviceName;

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

    /** 登记人姓名(用户不存在时为 null) */
    private String recorderName;

    /** 备注 */
    private String remark;

    /** 登记时间 */
    private LocalDateTime createTime;
}
