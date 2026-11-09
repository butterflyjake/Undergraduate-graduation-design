package com.campus.aed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 手动报修请求 DTO
 */
@Data
public class ReportRepairDTO {

    /** 设备ID(必填) */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /** 故障描述(必填) */
    @NotBlank(message = "故障描述不能为空")
    @Size(max = 500, message = "故障描述不能超过500个字符")
    private String faultDesc;

    /** 故障照片URL(选填) */
    @Size(max = 255, message = "故障照片URL不能超过255个字符")
    private String faultPhoto;
}
