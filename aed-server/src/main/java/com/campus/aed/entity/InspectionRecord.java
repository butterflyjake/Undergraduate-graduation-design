package com.campus.aed.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.aed.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 巡检记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inspection_record")
public class InspectionRecord extends BaseEntity {

    /** 关联任务ID */
    private Long taskId;

    /** 设备ID */
    private Long deviceId;

    /** 巡检人ID */
    private Long inspectorId;

    /** 巡检时间 */
    private LocalDateTime inspectTime;

    /** 巡检结果: 1-正常 2-异常 */
    private Integer result;

    /** 总体说明 */
    private String summary;
}
