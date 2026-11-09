package com.campus.aed.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 手动生成巡检任务请求 DTO
 */
@Data
public class GenerateTaskDTO {

    /** 计划巡检日期 */
    @NotNull(message = "计划日期不能为空")
    private LocalDate planDate;
}
