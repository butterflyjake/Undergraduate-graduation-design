package com.campus.aed.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 提交巡检记录请求 DTO
 */
@Data
public class SubmitInspectionDTO {

    /** 关联巡检任务ID */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    /** 总体说明 */
    private String summary;

    /** 检查明细列表, 固定6项 */
    @NotEmpty(message = "检查明细不能为空")
    @Size(min = 6, max = 6, message = "检查明细必须为固定6项")
    @Valid
    private List<InspectionItemDTO> items;
}
