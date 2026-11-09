package com.campus.aed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 驳回工单请求 DTO
 */
@Data
public class RejectRepairDTO {

    /** 驳回原因(必填) */
    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 200, message = "驳回原因不能超过200个字符")
    private String rejectReason;
}
