package com.campus.aed.service;

import com.campus.aed.vo.DashboardVO;

import java.util.List;

/**
 * 统计看板服务接口: 纯 SQL 聚合统计, 无对应实体
 */
public interface DashboardService {

    /**
     * 看板总览统计
     */
    DashboardVO.OverviewVO overview();

    /**
     * 设备状态分布统计
     */
    List<DashboardVO.StatusCountVO> deviceStatus();

    /**
     * 近N天巡检趋势统计(按计划巡检日期, 无数据日期补0)
     */
    List<DashboardVO.TrendVO> inspectionTrend(Integer days);

    /**
     * 近N月维修工单趋势统计(无数据月份补0)
     */
    List<DashboardVO.MonthCountVO> repairTrend(Integer months);

    /**
     * 近N月使用记录趋势统计(无数据月份补0)
     */
    List<DashboardVO.MonthCountVO> usageTrend(Integer months);

    /**
     * 设备楼宇分布统计
     */
    List<DashboardVO.BuildingDistVO> buildingDist();

    /**
     * 临期/过期配件清单
     */
    List<DashboardVO.ExpiringPartVO> expiringList();
}
