package com.campus.aed.controller;

import com.campus.aed.common.PageResult;
import com.campus.aed.common.Result;
import com.campus.aed.dto.HandleWarningDTO;
import com.campus.aed.dto.WarningQueryDTO;
import com.campus.aed.service.ExpiryWarningService;
import com.campus.aed.vo.WarningVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 到期预警控制器
 */
@RestController
@RequestMapping("/api/warning")
@RequiredArgsConstructor
@Tag(name = "到期预警")
public class ExpiryWarningController {

    private final ExpiryWarningService expiryWarningService;

    /**
     * 预警分页查询(所有登录用户可访问)
     */
    @GetMapping("/page")
    @Operation(summary = "预警分页查询")
    public Result<PageResult<WarningVO>> page(@Validated WarningQueryDTO dto) {
        return Result.ok(expiryWarningService.pageQuery(dto));
    }

    /**
     * 处理预警(管理员/设备管理员)
     */
    @PostMapping("/{id}/handle")
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    @Operation(summary = "处理预警")
    public Result<WarningVO> handle(@PathVariable("id") Long id,
                                    @Validated @RequestBody HandleWarningDTO dto) {
        return Result.ok(expiryWarningService.handle(id, dto));
    }
}
