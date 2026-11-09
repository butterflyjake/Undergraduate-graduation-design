package com.campus.aed.controller;

import com.campus.aed.common.Result;
import com.campus.aed.dto.BuildingDTO;
import com.campus.aed.entity.Building;
import com.campus.aed.service.BuildingService;
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
 * 楼宇管理控制器
 */
@RestController
@RequestMapping("/api/building")
@RequiredArgsConstructor
@Tag(name = "楼宇管理")
public class BuildingController {

    private final BuildingService buildingService;

    /**
     * 楼宇列表(不分页, 所有登录用户可访问)
     */
    @GetMapping("/list")
    @Operation(summary = "楼宇列表(不分页)")
    public Result<List<Building>> list() {
        return Result.ok(buildingService.listAll());
    }

    /**
     * 新增楼宇
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    @Operation(summary = "新增楼宇")
    public Result<Void> create(@Valid @RequestBody BuildingDTO dto) {
        buildingService.create(dto);
        return Result.ok();
    }

    /**
     * 修改楼宇
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    @Operation(summary = "修改楼宇")
    public Result<Void> update(@Valid @RequestBody BuildingDTO dto) {
        buildingService.update(dto);
        return Result.ok();
    }

    /**
     * 删除楼宇
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    @Operation(summary = "删除楼宇")
    public Result<Void> delete(@PathVariable Long id) {
        buildingService.delete(id);
        return Result.ok();
    }
}
