package com.campus.aed.dto;

import com.campus.aed.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 使用记录分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UsageQueryDTO extends PageQuery {

    /** 设备ID过滤(可选, 不传则查询全部) */
    private Long deviceId;
}
