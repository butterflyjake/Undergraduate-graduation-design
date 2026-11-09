package com.campus.aed.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 处理预警请求参数
 */
@Data
@Schema(description = "处理预警请求参数")
public class HandleWarningDTO {

    /** 处理说明 */
    @Schema(description = "处理说明", example = "已采购新电极片, 本周更换")
    @NotBlank(message = "处理说明不能为空")
    @Size(max = 200, message = "处理说明长度不能超过200字")
    private String handleDesc;
}
