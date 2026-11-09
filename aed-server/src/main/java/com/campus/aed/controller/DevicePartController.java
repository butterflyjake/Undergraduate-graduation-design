package com.campus.aed.controller;

import com.campus.aed.common.PageResult;
import com.campus.aed.common.Result;
import com.campus.aed.dto.PartDTO;
import com.campus.aed.dto.PartQueryDTO;
import com.campus.aed.service.DevicePartService;
import com.campus.aed.vo.PartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * 配件管理控制器: 电极片/电池等设备配件的台账管理
 */
@RestController
@RequestMapping("/api/part")
@RequiredArgsConstructor
@Tag(name = "配件管理")
public class DevicePartController {

    private final DevicePartService devicePartService;

    /** 配件分页查询(所有登录用户可查看) */
    @GetMapping("/page")
    @Operation(summary = "配件分页查询")
    public Result<PageResult<PartVO>> page(PartQueryDTO dto) {
        return Result.ok(devicePartService.pageQuery(dto));
    }

    /** 某设备配件列表(不分页, 所有登录用户可查看) */
    @GetMapping("/list")
    @Operation(summary = "某设备配件列表")
    public Result<List<PartVO>> list(PartQueryDTO dto) {
        return Result.ok(devicePartService.listQuery(dto));
    }

    /** 新增配件(自动替换旧配件, 仅管理员/设备管理员) */
    @PostMapping
    @Operation(summary = "新增配件")
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    public Result<PartVO> add(@RequestBody @Valid PartDTO dto) {
        return Result.ok(devicePartService.add(dto));
    }

    /** 修改配件(仅管理员/设备管理员) */
    @PutMapping
    @Operation(summary = "修改配件")
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    public Result<PartVO> update(@RequestBody @Valid PartDTO dto) {
        return Result.ok(devicePartService.update(dto));
    }

    /** 删除配件(逻辑删除, 仅管理员/设备管理员) */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除配件")
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    public Result<Void> delete(@PathVariable Long id) {
        devicePartService.delete(id);
        return Result.ok();
    }
}
