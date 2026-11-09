package com.campus.aed.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.LogQueryDTO;
import com.campus.aed.entity.OperationLog;

/**
 * 操作日志服务(日志由切面写入, 仅提供查询)
 */
public interface LogService extends IService<OperationLog> {

    /** 操作日志分页查询(按操作时间倒序) */
    PageResult<OperationLog> pageQuery(LogQueryDTO dto);
}
