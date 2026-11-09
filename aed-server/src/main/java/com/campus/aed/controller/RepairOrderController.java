package com.campus.aed.controller;

import com.campus.aed.common.PageResult;
import com.campus.aed.common.Result;
import com.campus.aed.dto.CompleteRepairDTO;
import com.campus.aed.dto.RejectRepairDTO;
import com.campus.aed.dto.RepairQueryDTO;
import com.campus.aed.dto.ReportRepairDTO;
import com.campus.aed.service.RepairOrderService;
import com.campus.aed.vo.RepairOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * 维修工单控制器
 */
@RestController
@RequestMapping("/api/repair")
@RequiredArgsConstructor
@Validated
@Tag(name = "维修工单")
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    /**
     * 工单分页查询(所有登录用户)
     */
    @GetMapping("/page")
    @Operation(summary = "工单分页查询")
    public Result<PageResult<RepairOrderVO>> page(@Validated RepairQueryDTO dto) {
        return Result.ok(repairOrderService.pageQuery(dto));
    }

    /**
     * 手动报修(所有登录用户)
     */
    @PostMapping
    @Operation(summary = "手动报修")
    public Result<RepairOrderVO> report(@RequestBody @Valid ReportRepairDTO dto) {
        return Result.ok(repairOrderService.report(dto));
    }

    /**
     * 受理工单(管理员/设备管理员)
     */
    @PostMapping("/{id}/accept")
    @Operation(summary = "受理工单")
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    public Result<Void> accept(@PathVariable Long id) {
        repairOrderService.accept(id);
        return Result.ok();
    }

    /**
     * 完成工单(管理员/设备管理员)
     */
    @PostMapping("/{id}/complete")
    @Operation(summary = "完成工单")
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    public Result<Void> complete(@PathVariable Long id, @RequestBody @Valid CompleteRepairDTO dto) {
        repairOrderService.complete(id, dto);
        return Result.ok();
    }

    /**
     * 驳回工单(管理员/设备管理员)
     */
    @PostMapping("/{id}/reject")
    @Operation(summary = "驳回工单")
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    public Result<Void> reject(@PathVariable Long id, @RequestBody @Valid RejectRepairDTO dto) {
        repairOrderService.reject(id, dto);
        return Result.ok();
    }

    /**
     * 工单详情(所有登录用户)
     */
    @GetMapping("/{id}")
    @Operation(summary = "工单详情")
    public Result<RepairOrderVO> detail(@PathVariable Long id) {
        return Result.ok(repairOrderService.detail(id));
    }
}
