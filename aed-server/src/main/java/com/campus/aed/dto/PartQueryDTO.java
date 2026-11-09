package com.campus.aed.dto;

import com.campus.aed.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配件分页/列表查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PartQueryDTO extends PageQuery {

    /** 所属设备ID */
    private Long deviceId;

    /** 配件类型: 1-电极片 2-电池 */
    private Integer partType;

    /** 状态: 0-使用中 1-已更换 */
    private Integer status;
}
