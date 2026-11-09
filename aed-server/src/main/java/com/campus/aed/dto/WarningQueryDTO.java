package com.campus.aed.dto;

import com.campus.aed.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预警分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "预警分页查询参数")
public class WarningQueryDTO extends PageQuery {

    /** 处理状态: 0-未处理 1-已处理(可空, 查全部) */
    @Schema(description = "处理状态: 0-未处理 1-已处理, 可空查全部")
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 1, message = "状态值不合法")
    private Integer status;

    /** 预警类型: 1-临期 2-已过期(可空, 查全部) */
    @Schema(description = "预警类型: 1-临期 2-已过期, 可空查全部")
    @Min(value = 1, message = "预警类型不合法")
    @Max(value = 2, message = "预警类型不合法")
    private Integer warningType;
}
