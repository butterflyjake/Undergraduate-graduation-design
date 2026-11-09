package com.campus.aed.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.aed.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 维修工单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("repair_order")
public class RepairOrder extends BaseEntity {

    /** 工单编号, 如 WX20261028153000123 */
    private String orderNo;

    /** 设备ID */
    private Long deviceId;

    /** 报修来源: 1-巡检异常 2-手动报修 */
    private Integer reportType;

    /** 报修人ID */
    private Long reporterId;

    /** 故障描述 */
    private String faultDesc;

    /** 故障照片URL */
    private String faultPhoto;

    /** 状态: 0-待处理 1-维修中 2-已完成 3-已驳回 */
    private Integer status;

    /** 处理人ID */
    private Long handlerId;

    /** 维修说明 */
    private String repairDesc;

    /** 维修费用(元) */
    private BigDecimal repairCost;

    /** 完成维修时间 */
    private LocalDateTime repairTime;

    /** 驳回原因 */
    private String rejectReason;
}
