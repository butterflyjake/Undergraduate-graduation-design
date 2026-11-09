package com.campus.aed.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.aed.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 巡检明细实体(逐项检查结果)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inspection_item")
public class InspectionItem extends BaseEntity {

    /** 关联巡检记录ID */
    private Long recordId;

    /** 检查项名称 */
    private String itemName;

    /** 检查结果: 1-正常 2-异常 */
    private Integer itemResult;

    /** 异常说明 */
    private String remark;
}
