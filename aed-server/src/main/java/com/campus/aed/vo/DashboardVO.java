package com.campus.aed.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计看板视图对象: 各接口返回结构以静态内部类定义
 */
public final class DashboardVO {

    /** 容器类, 禁止实例化 */
    private DashboardVO() {
    }

    /**
     * 看板总览统计
     */
    @Data
    public static class OverviewVO {

        /** 设备总数(全部状态) */
        private Long deviceTotal;

        /** 在用设备数 */
        private Long inUseCount;

        /** 待巡检任务数 */
        private Long pendingTaskCount;

        /** 逾期任务数 */
        private Long overdueTaskCount;

        /** 待处理维修工单数 */
        private Long pendingRepairCount;

        /** 未处理预警数 */
        private Long pendingWarningCount;

        /** 已过期配件数(仅使用中) */
        private Long expiredPartCount;
    }

    /**
     * 设备状态分布统计
     */
    @Data
    public static class StatusCountVO {

        /** 状态编码: 0-在用 1-维修中 2-停用 3-已报废 */
        private Integer status;

        /** 状态名称 */
        private String name;

        /** 数量 */
        private Long count;
    }

    /**
     * 巡检趋势统计(按天)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendVO {

        /** 日期 yyyy-MM-dd */
        private String date;

        /** 当日任务总数 */
        private Long total;

        /** 当日完成数 */
        private Long done;
    }

    /**
     * 按月趋势统计(维修/使用)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthCountVO {

        /** 月份 yyyy-MM */
        private String month;

        /** 数量 */
        private Long count;
    }

    /**
     * 设备楼宇分布统计
     */
    @Data
    public static class BuildingDistVO {

        /** 楼宇名称, 未关联楼宇为"未指定" */
        private String buildingName;

        /** 设备数量 */
        private Long count;
    }

    /**
     * 临期/过期配件
     */
    @Data
    public static class ExpiringPartVO {

        /** 设备编号 */
        private String deviceCode;

        /** 设备名称 */
        private String deviceName;

        /** 配件类型: 1-电极片 2-电池 */
        private Integer partType;

        /** 有效期至 yyyy-MM-dd */
        private String expireDate;

        /** 剩余天数, 已过期为负数 */
        private Integer daysRemaining;
    }
}
