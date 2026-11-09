package com.campus.aed.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 巡检明细项 DTO(提交巡检记录时逐项填写)
 */
@Data
public class InspectionItemDTO {

    /** 检查项名称 */
    @NotBlank(message = "检查项名称不能为空")
    private String itemName;

    /** 检查结果: 1-正常 2-异常 */
    @NotNull(message = "检查结果不能为空")
    @Min(value = 1, message = "检查结果只能为1-正常或2-异常")
    @Max(value = 2, message = "检查结果只能为1-正常或2-异常")
    private Integer itemResult;

    /** 异常说明 */
    private String remark;
}
