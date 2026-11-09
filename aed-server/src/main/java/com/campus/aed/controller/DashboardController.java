package com.campus.aed.controller;

import com.campus.aed.common.Result;
import com.campus.aed.service.DashboardService;
import com.campus.aed.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统计看板控制器: 总览卡片、状态分布、趋势统计与临期清单
 * 全部接口登录即可访问, 无需额外权限
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Validated
@Tag(name = "统计看板")
public class DashboardController {

    private final DashboardService dashboardService;

    /** 看板总览统计 */
    @GetMapping("/overview")
    @Operation(summary = "看板总览统计")
    public Result<DashboardVO.OverviewVO> overview() {
        return Result.ok(dashboardService.overview());
    }

    /** 设备状态分布统计(饼图) */
    @GetMapping("/deviceStatus")
    @Operation(summary = "设备状态分布统计")
    public Result<List<DashboardVO.StatusCountVO>> deviceStatus() {
        return Result.ok(dashboardService.deviceStatus());
    }

    /** 近N天巡检趋势统计(折线), 默认近7天 */
    @GetMapping("/inspectionTrend")
    @Operation(summary = "近N天巡检趋势统计")
    public Result<List<DashboardVO.TrendVO>> inspectionTrend(
            @RequestParam(defaultValue = "7") @Min(value = 1, message = "统计天数最小为1天") Integer days) {
        return Result.ok(dashboardService.inspectionTrend(days));
    }

    /** 近N月维修工单趋势统计(柱状), 默认近6个月 */
    @GetMapping("/repairTrend")
    @Operation(summary = "近N月维修工单趋势统计")
    public Result<List<DashboardVO.MonthCountVO>> repairTrend(
            @RequestParam(defaultValue = "6") @Min(value = 1, message = "统计月数最小为1个月") Integer months) {
        return Result.ok(dashboardService.repairTrend(months));
    }

    /** 近N月使用记录趋势统计(柱状), 默认近6个月 */
    @GetMapping("/usageTrend")
    @Operation(summary = "近N月使用记录趋势统计")
    public Result<List<DashboardVO.MonthCountVO>> usageTrend(
            @RequestParam(defaultValue = "6") @Min(value = 1, message = "统计月数最小为1个月") Integer months) {
        return Result.ok(dashboardService.usageTrend(months));
    }

    /** 设备楼宇分布统计(柱状) */
    @GetMapping("/buildingDist")
    @Operation(summary = "设备楼宇分布统计")
    public Result<List<DashboardVO.BuildingDistVO>> buildingDist() {
        return Result.ok(dashboardService.buildingDist());
    }

    /** 临期/过期配件清单 */
    @GetMapping("/expiringList")
    @Operation(summary = "临期/过期配件清单")
    public Result<List<DashboardVO.ExpiringPartVO>> expiringList() {
        return Result.ok(dashboardService.expiringList());
    }
}
