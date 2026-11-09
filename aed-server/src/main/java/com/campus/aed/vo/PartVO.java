package com.campus.aed.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 配件信息返回对象(增加 deviceCode/deviceName 设备信息)
 */
@Data
public class PartVO {

    /** 配件ID */
    private Long id;

    /** 所属设备ID */
    private Long deviceId;

    /** 设备编号(设备已删除时为 null) */
    private String deviceCode;

    /** 设备型号(设备已删除时为 null) */
    private String deviceName;

    /** 配件类型: 1-电极片 2-电池 */
    private Integer partType;

    /** 批次号 */
    private String batchNo;

    /** 安装日期 */
    private LocalDate installDate;

    /** 有效期至 */
    private LocalDate expireDate;

    /** 状态: 0-使用中 1-已更换 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
