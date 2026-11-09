package com.campus.aed.common;

import lombok.Data;

/**
 * 分页查询参数基类, 各模块查询 DTO 继承此类
 */
@Data
public class PageQuery {

    /** 页码, 从 1 开始 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;
}
