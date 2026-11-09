package com.campus.aed.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * 设备台账新增/修改请求参数
 */
@Data
public class DeviceDTO {

    /** 设备ID(新增时不传, 修改时必传) */
    private Long id;

    /** 设备编号, 如 AED-001 */
    @NotBlank(message = "设备编号不能为空")
    private String deviceCode;

    /** 设备型号 */
    @NotBlank(message = "设备名称不能为空")
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
    @Min(value = 1, message = "使用年限必须大于等于1年")
    private Integer serviceLife;

    /** 所在楼宇ID */
    private Long buildingId;

    /** 所在楼层 */
    private String floor;

    /** 具体位置描述 */
    private String positionDesc;

    /** 状态: 0-在用 1-维修中 2-停用 3-已报废 */
    @Min(value = 0, message = "状态值不合法, 必须在0-3之间")
    @Max(value = 3, message = "状态值不合法, 必须在0-3之间")
    private Integer status;

    /** 设备照片URL */
    private String photo;

    /** 备注 */
    private String remark;
}
