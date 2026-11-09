package com.campus.aed.controller;

import com.campus.aed.common.PageResult;
import com.campus.aed.common.Result;
import com.campus.aed.dto.LogQueryDTO;
import com.campus.aed.entity.OperationLog;
import com.campus.aed.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志控制器: 仅提供分页查询(仅系统管理员可操作)
 */
@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
@Tag(name = "操作日志")
public class LogController {

    private final LogService logService;

    /** 操作日志分页查询: keyword 模糊匹配用户名或操作描述, 按操作时间倒序 */
    @GetMapping("/page")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "操作日志分页查询")
    public Result<PageResult<OperationLog>> page(LogQueryDTO dto) {
        return Result.ok(logService.pageQuery(dto));
    }
}
