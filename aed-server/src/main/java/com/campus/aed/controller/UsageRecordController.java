package com.campus.aed.controller;

import com.campus.aed.common.PageResult;
import com.campus.aed.common.Result;
import com.campus.aed.dto.UsageDTO;
import com.campus.aed.dto.UsageQueryDTO;
import com.campus.aed.service.UsageRecordService;
import com.campus.aed.vo.UsageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AED使用记录控制器
 */
@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
@Tag(name = "使用记录")
public class UsageRecordController {

    private final UsageRecordService usageRecordService;

    /** 使用记录分页查询(全部登录用户, 支持 deviceId 过滤) */
    @GetMapping("/page")
    @Operation(summary = "使用记录分页查询")
    public Result<PageResult<UsageVO>> page(UsageQueryDTO dto) {
        return Result.ok(usageRecordService.pageQuery(dto));
    }

    /** 登记使用记录(全部登录用户, 登记人取当前登录用户) */
    @PostMapping
    @Operation(summary = "登记使用记录")
    public Result<UsageVO> add(@Valid @RequestBody UsageDTO dto) {
        return Result.ok(usageRecordService.add(dto));
    }

    /** 使用记录详情(全部登录用户) */
    @GetMapping("/{id}")
    @Operation(summary = "使用记录详情")
    public Result<UsageVO> detail(@PathVariable Long id) {
        return Result.ok(usageRecordService.getDetail(id));
    }
}
