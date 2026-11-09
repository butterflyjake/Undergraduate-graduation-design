package com.campus.aed.dto;

import com.campus.aed.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 巡检记录分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InspectionRecordQueryDTO extends PageQuery {

    /** 设备ID(可选过滤条件) */
    private Long deviceId;

    /** 巡检结果: 1-正常 2-异常(可选过滤条件) */
    private Integer result;
}
