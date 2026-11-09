package com.campus.aed.controller;

import com.campus.aed.common.PageResult;
import com.campus.aed.common.Result;
import com.campus.aed.dto.InspectionRecordQueryDTO;
import com.campus.aed.dto.SubmitInspectionDTO;
import com.campus.aed.service.InspectionRecordService;
import com.campus.aed.vo.InspectionRecordDetailVO;
import com.campus.aed.vo.InspectionRecordVO;
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
 * 巡检记录控制器
 */
@RestController
@RequestMapping("/api/inspection/record")
@RequiredArgsConstructor
@Tag(name = "巡检记录")
public class InspectionRecordController {

    private final InspectionRecordService inspectionRecordService;

    /** 提交巡检记录(仅巡检人员), 存在异常项时自动生成维修工单 */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('inspector')")
    @Operation(summary = "提交巡检记录")
    public Result<Void> submit(@Valid @RequestBody SubmitInspectionDTO dto) {
        inspectionRecordService.submit(dto);
        return Result.ok();
    }

    /** 巡检记录分页查询(inspector 角色仅查本人记录) */
    @GetMapping("/page")
    @Operation(summary = "巡检记录分页查询")
    public Result<PageResult<InspectionRecordVO>> page(InspectionRecordQueryDTO dto) {
        return Result.ok(inspectionRecordService.pageQuery(dto));
    }

    /** 巡检记录详情(含逐项检查明细) */
    @GetMapping("/{id}")
    @Operation(summary = "巡检记录详情")
    public Result<InspectionRecordDetailVO> detail(@PathVariable Long id) {
        return Result.ok(inspectionRecordService.getDetail(id));
    }
}
