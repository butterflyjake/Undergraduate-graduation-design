package com.campus.aed.dto;

import com.campus.aed.common.PageQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备台账分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceQueryDTO extends PageQuery {

    /** 关键字: 模糊匹配设备编号或设备名称 */
    private String keyword;

    /** 所在楼宇ID过滤 */
    private Long buildingId;

    /** 状态过滤: 0-在用 1-维修中 2-停用 3-已报废 */
    @Min(value = 0, message = "状态值不合法, 必须在0-3之间")
    @Max(value = 3, message = "状态值不合法, 必须在0-3之间")
    private Integer status;
}
