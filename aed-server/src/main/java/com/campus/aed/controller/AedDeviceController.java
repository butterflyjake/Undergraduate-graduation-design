package com.campus.aed.controller;

import com.campus.aed.common.PageResult;
import com.campus.aed.common.Result;
import com.campus.aed.dto.DeviceDTO;
import com.campus.aed.dto.DeviceQueryDTO;
import com.campus.aed.entity.AedDevice;
import com.campus.aed.service.AedDeviceService;
import com.campus.aed.vo.DeviceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 设备台账控制器
 */
@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
@Tag(name = "设备台账")
public class AedDeviceController {

    private final AedDeviceService deviceService;

    @GetMapping("/page")
    @Operation(summary = "设备分页查询")
    public Result<PageResult<DeviceVO>> page(@Valid DeviceQueryDTO dto) {
        return Result.ok(deviceService.pageQuery(dto));
    }

    @GetMapping("/list")
    @Operation(summary = "设备简表(下拉选择用, 不分页)")
    public Result<List<AedDevice>> list() {
        return Result.ok(deviceService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "设备详情")
    public Result<DeviceVO> detail(@PathVariable Long id) {
        return Result.ok(deviceService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    @Operation(summary = "新增设备")
    public Result<DeviceVO> create(@Valid @RequestBody DeviceDTO dto) {
        return Result.ok(deviceService.create(dto));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    @Operation(summary = "修改设备")
    public Result<DeviceVO> update(@Valid @RequestBody DeviceDTO dto) {
        return Result.ok(deviceService.update(dto));
    }

    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    @Operation(summary = "快速变更设备状态")
    public Result<Void> updateStatus(@Valid @RequestBody StatusUpdateDTO dto) {
        deviceService.updateStatus(dto.getId(), dto.getStatus());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    @Operation(summary = "删除设备")
    public Result<Void> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return Result.ok();
    }

    /**
     * 快速变更状态请求参数
     */
    @Data
    public static class StatusUpdateDTO {

        /** 设备ID */
        @NotNull(message = "设备ID不能为空")
        private Long id;

        /** 状态: 0-在用 1-维修中 2-停用 3-已报废 */
        @NotNull(message = "状态不能为空")
        @Min(value = 0, message = "状态值不合法, 必须在0-3之间")
        @Max(value = 3, message = "状态值不合法, 必须在0-3之间")
        private Integer status;
    }
}
