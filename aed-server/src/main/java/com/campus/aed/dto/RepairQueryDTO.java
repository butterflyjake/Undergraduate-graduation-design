package com.campus.aed.dto;

import com.campus.aed.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 维修工单分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RepairQueryDTO extends PageQuery {

    /** 状态过滤: 0-待处理 1-维修中 2-已完成 3-已驳回(选填) */
    private Integer status;

    /** 设备ID过滤(选填) */
    private Long deviceId;
}
