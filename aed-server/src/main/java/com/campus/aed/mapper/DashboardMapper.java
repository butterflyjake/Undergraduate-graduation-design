package com.campus.aed.mapper;

import com.campus.aed.vo.DashboardVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 统计看板 Mapper: 全部通过 @Select 注解 SQL 聚合统计, 不依赖实体
 * 涉及表: aed_device, device_part, inspection_task, repair_order, usage_record, expiry_warning, building
 */
public interface DashboardMapper {

    /**
     * 看板总览: 设备总数与在用设备数
     */
    @Select("SELECT COUNT(*) AS deviceTotal, " +
            "IFNULL(SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END), 0) AS inUseCount " +
            "FROM aed_device WHERE deleted = 0")
    DashboardVO.OverviewVO selectOverview();

    /**
     * 按状态统计巡检任务数(0-待巡检 2-已逾期)
     */
    @Select("SELECT COUNT(*) FROM inspection_task WHERE deleted = 0 AND status = #{status}")
    Long countInspectionTask(@Param("status") Integer status);

    /**
     * 待处理维修工单数(status=0)
     */
    @Select("SELECT COUNT(*) FROM repair_order WHERE deleted = 0 AND status = 0")
    Long countPendingRepair();

    /**
     * 未处理预警数(status=0)
     */
    @Select("SELECT COUNT(*) FROM expiry_warning WHERE deleted = 0 AND status = 0")
    Long countPendingWarning();

    /**
     * 已过期配件数(使用中且已过有效期)
     */
    @Select("SELECT COUNT(*) FROM device_part WHERE deleted = 0 AND status = 0 AND expire_date < CURDATE()")
    Long countExpiredPart();

    /**
     * 设备状态分布(饼图): 全部状态的设备按状态分组统计
     */
    @Select("SELECT status, CASE status WHEN 0 THEN '在用' WHEN 1 THEN '维修中' " +
            "WHEN 2 THEN '停用' WHEN 3 THEN '已报废' ELSE '未知' END AS name, COUNT(*) AS count " +
            "FROM aed_device WHERE deleted = 0 GROUP BY status ORDER BY status")
    List<DashboardVO.StatusCountVO> selectDeviceStatus();

    /**
     * 近N天巡检趋势(折线): 按计划巡检日期分组, 统计当日任务总数与完成数
     * 统计范围为今天及前N-1天(共N个日期), 即 (今天-N, 今天]
     */
    @Select("SELECT DATE_FORMAT(plan_date, '%Y-%m-%d') AS date, COUNT(*) AS total, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS done " +
            "FROM inspection_task WHERE deleted = 0 " +
            "AND plan_date > DATE_SUB(CURDATE(), INTERVAL #{days} DAY) AND plan_date <= CURDATE() " +
            "GROUP BY DATE_FORMAT(plan_date, '%Y-%m-%d') ORDER BY date")
    List<DashboardVO.TrendVO> selectInspectionTrend(@Param("days") Integer days);

    /**
     * 近N月维修工单趋势(柱状): 按创建月份分组统计
     * 统计范围为当前月及前N-1个整月(共N个月), 即从 N-1 个月前的月初开始
     */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS month, COUNT(*) AS count " +
            "FROM repair_order WHERE deleted = 0 " +
            "AND create_time >= LAST_DAY(DATE_SUB(CURDATE(), INTERVAL #{months} MONTH)) + INTERVAL 1 DAY " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') ORDER BY month")
    List<DashboardVO.MonthCountVO> selectRepairTrend(@Param("months") Integer months);

    /**
     * 近N月使用记录趋势(柱状): 按创建月份分组统计
     * 统计范围为当前月及前N-1个整月(共N个月), 即从 N-1 个月前的月初开始
     */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m') AS month, COUNT(*) AS count " +
            "FROM usage_record WHERE deleted = 0 " +
            "AND create_time >= LAST_DAY(DATE_SUB(CURDATE(), INTERVAL #{months} MONTH)) + INTERVAL 1 DAY " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m') ORDER BY month")
    List<DashboardVO.MonthCountVO> selectUsageTrend(@Param("months") Integer months);

    /**
     * 设备楼宇分布(柱状): 按楼宇分组统计全部状态设备数, 未关联楼宇显示"未指定"
     */
    @Select("SELECT IFNULL(b.building_name, '未指定') AS buildingName, COUNT(*) AS count " +
            "FROM aed_device d LEFT JOIN building b ON d.building_id = b.id AND b.deleted = 0 " +
            "WHERE d.deleted = 0 GROUP BY IFNULL(b.building_name, '未指定') ORDER BY count DESC")
    List<DashboardVO.BuildingDistVO> selectBuildingDist();

    /**
     * 临期/过期配件清单: 使用中配件30天内到期(含已过期), 按到期日升序取前20条
     */
    @Select("SELECT d.device_code AS deviceCode, d.device_name AS deviceName, p.part_type AS partType, " +
            "DATE_FORMAT(p.expire_date, '%Y-%m-%d') AS expireDate, " +
            "DATEDIFF(p.expire_date, CURDATE()) AS daysRemaining " +
            "FROM device_part p LEFT JOIN aed_device d ON p.device_id = d.id AND d.deleted = 0 " +
            "WHERE p.deleted = 0 AND p.status = 0 AND p.expire_date <= DATE_ADD(CURDATE(), INTERVAL 30 DAY) " +
            "ORDER BY p.expire_date ASC LIMIT 20")
    List<DashboardVO.ExpiringPartVO> selectExpiringList();
}
