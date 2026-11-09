package com.campus.aed.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 维修工单 VO(在实体基础上补充 deviceCode/deviceName/reporterName/handlerName)
 */
@Data
public class RepairOrderVO {

    /** 工单ID */
    private Long id;

    /** 工单编号, 如 WX20261028153000123 */
    private String orderNo;

    /** 设备ID */
    private Long deviceId;

    /** 设备编号(冗余填充, 设备不存在时为 null) */
    private String deviceCode;

    /** 设备型号(冗余填充, 设备不存在时为 null) */
    private String deviceName;

    /** 报修来源: 1-巡检异常 2-手动报修 */
    private Integer reportType;

    /** 报修人ID */
    private Long reporterId;

    /** 报修人姓名(冗余填充, 用户不存在时为 null) */
    private String reporterName;

    /** 故障描述 */
    private String faultDesc;

    /** 故障照片URL */
    private String faultPhoto;

    /** 状态: 0-待处理 1-维修中 2-已完成 3-已驳回 */
    private Integer status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理人姓名(冗余填充, 未受理或用户不存在时为 null) */
    private String handlerName;

    /** 维修说明 */
    private String repairDesc;

    /** 维修费用(元) */
    private BigDecimal repairCost;

    /** 完成维修时间 */
    private LocalDateTime repairTime;

    /** 驳回原因 */
    private String rejectReason;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
