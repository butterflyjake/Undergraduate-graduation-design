package com.campus.aed.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 楼宇新增/修改请求参数
 */
@Data
public class BuildingDTO {

    /** 楼宇ID(修改时必填, 新增时忽略) */
    private Long id;

    /** 楼宇名称 */
    @NotBlank(message = "楼宇名称不能为空")
    private String buildingName;

    /** 所属校区 */
    private String campusArea;

    /** 位置描述 */
    private String address;

    /** 备注 */
    private String remark;
}
