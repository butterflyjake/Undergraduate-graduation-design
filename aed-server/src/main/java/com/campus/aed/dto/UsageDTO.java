package com.campus.aed.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登记使用记录请求 DTO
 */
@Data
public class UsageDTO {

    /** 设备ID(必填) */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /** 使用发生时间(必填) */
    @NotNull(message = "使用发生时间不能为空")
    private LocalDateTime occurTime;

    /** 事发地点描述 */
    @Size(max = 200, message = "事发地点描述不能超过200个字符")
    private String locationDesc;

    /** 患者情况(匿名描述) */
    @Size(max = 200, message = "患者情况描述不能超过200个字符")
    private String patientDesc;

    /** 处置结果 */
    @Size(max = 500, message = "处置结果不能超过500个字符")
    private String resultDesc;

    /** 备注 */
    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
