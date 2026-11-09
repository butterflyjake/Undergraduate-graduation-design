package com.campus.aed.service.impl;

import com.campus.aed.mapper.DashboardMapper;
import com.campus.aed.service.DashboardService;
import com.campus.aed.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统计看板服务实现: 纯 SQL 聚合统计, 日期/月份序列在 Java 层补全
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    /** 月份格式化: yyyy-MM */
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final DashboardMapper dashboardMapper;

    @Override
    public DashboardVO.OverviewVO overview() {
        DashboardVO.OverviewVO overview = dashboardMapper.selectOverview();
        overview.setPendingTaskCount(dashboardMapper.countInspectionTask(0));
        overview.setOverdueTaskCount(dashboardMapper.countInspectionTask(2));
        overview.setPendingRepairCount(dashboardMapper.countPendingRepair());
        overview.setPendingWarningCount(dashboardMapper.countPendingWarning());
        overview.setExpiredPartCount(dashboardMapper.countExpiredPart());
        return overview;
    }

    @Override
    public List<DashboardVO.StatusCountVO> deviceStatus() {
        return dashboardMapper.selectDeviceStatus();
    }

    @Override
    public List<DashboardVO.TrendVO> inspectionTrend(Integer days) {
        Map<String, DashboardVO.TrendVO> byDate = dashboardMapper.selectInspectionTrend(days).stream()
                .collect(Collectors.toMap(DashboardVO.TrendVO::getDate, Function.identity()));
        // 按 SQL 统计范围 [今天-days+1, 今天] 补全日期序列, 无数据日期补 0
        List<DashboardVO.TrendVO> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (LocalDate day = today.minusDays(days - 1); !day.isAfter(today); day = day.plusDays(1)) {
            String key = day.toString();
            result.add(byDate.getOrDefault(key, new DashboardVO.TrendVO(key, 0L, 0L)));
        }
        return result;
    }

    @Override
    public List<DashboardVO.MonthCountVO> repairTrend(Integer months) {
        return fillMonthSequence(dashboardMapper.selectRepairTrend(months), months);
    }

    @Override
    public List<DashboardVO.MonthCountVO> usageTrend(Integer months) {
        return fillMonthSequence(dashboardMapper.selectUsageTrend(months), months);
    }

    @Override
    public List<DashboardVO.BuildingDistVO> buildingDist() {
        return dashboardMapper.selectBuildingDist();
    }

    @Override
    public List<DashboardVO.ExpiringPartVO> expiringList() {
        return dashboardMapper.selectExpiringList();
    }

    /**
     * 按 SQL 统计范围 [当前月-months+1, 当前月] 补全月份序列, 无数据月份补 0
     */
    private List<DashboardVO.MonthCountVO> fillMonthSequence(List<DashboardVO.MonthCountVO> records, Integer months) {
        Map<String, DashboardVO.MonthCountVO> byMonth = records.stream()
                .collect(Collectors.toMap(DashboardVO.MonthCountVO::getMonth, Function.identity()));
        List<DashboardVO.MonthCountVO> result = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (YearMonth ym = current.minusMonths(months - 1); !ym.isAfter(current); ym = ym.plusMonths(1)) {
            String key = ym.format(MONTH_FORMATTER);
            result.add(byMonth.getOrDefault(key, new DashboardVO.MonthCountVO(key, 0L)));
        }
        return result;
    }
}
