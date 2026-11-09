package com.campus.aed.controller;

import com.campus.aed.common.PageResult;
import com.campus.aed.common.Result;
import com.campus.aed.dto.GenerateTaskDTO;
import com.campus.aed.dto.InspectionTaskQueryDTO;
import com.campus.aed.service.InspectionTaskService;
import com.campus.aed.vo.InspectionTaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 巡检任务控制器
 */
@RestController
@RequestMapping("/api/inspection/task")
@RequiredArgsConstructor
@Tag(name = "巡检任务")
public class InspectionTaskController {

    private final InspectionTaskService inspectionTaskService;

    /** 巡检任务分页查询(inspector 角色仅查本人任务) */
    @GetMapping("/page")
    @Operation(summary = "巡检任务分页查询")
    public Result<PageResult<InspectionTaskVO>> page(InspectionTaskQueryDTO dto) {
        return Result.ok(inspectionTaskService.pageQuery(dto));
    }

    /** 手动生成指定日期的巡检任务 */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('admin','device_manager')")
    @Operation(summary = "手动生成巡检任务")
    public Result<Integer> generate(@Valid @RequestBody GenerateTaskDTO dto) {
        return Result.ok(inspectionTaskService.generateTasks(dto.getPlanDate()));
    }

    /** 巡检任务详情 */
    @GetMapping("/{id}")
    @Operation(summary = "巡检任务详情")
    public Result<InspectionTaskVO> detail(@PathVariable Long id) {
        return Result.ok(inspectionTaskService.getDetail(id));
    }
}
