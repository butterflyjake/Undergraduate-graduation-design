package com.campus.aed.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.aed.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 楼宇实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("building")
public class Building extends BaseEntity {

    /** 楼宇名称 */
    private String buildingName;

    /** 所属校区 */
    private String campusArea;

    /** 位置描述 */
    private String address;

    /** 备注 */
    private String remark;
}
