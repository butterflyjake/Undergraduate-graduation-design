package com.campus.aed.dto;

import com.campus.aed.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LogQueryDTO extends PageQuery {

    /** 关键字: 模糊匹配操作用户名或操作描述 */
    private String keyword;
}
