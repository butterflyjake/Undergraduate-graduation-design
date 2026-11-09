package com.campus.aed.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备台账视图对象
 */
@Data
public class DeviceVO {

    /** 设备ID */
    private Long id;

    /** 设备编号, 如 AED-001 */
    private String deviceCode;

    /** 设备型号 */
    private String deviceName;

    /** 品牌 */
    private String brand;

    /** 序列号 */
    private String serialNo;

    /** 生产厂家 */
    private String manufacturer;

    /** 购置日期 */
    private LocalDate purchaseDate;

    /** 设计使用年限(年) */
    private Integer serviceLife;

    /** 所在楼宇ID */
    private Long buildingId;

    /** 所在楼宇名称(关联楼宇表填充, 楼宇为空或不存在时为 null) */
    private String buildingName;

    /** 所在楼层 */
    private String floor;

    /** 具体位置描述 */
    private String positionDesc;

    /** 状态: 0-在用 1-维修中 2-停用 3-已报废 */
    private Integer status;

    /** 设备照片URL */
    private String photo;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
