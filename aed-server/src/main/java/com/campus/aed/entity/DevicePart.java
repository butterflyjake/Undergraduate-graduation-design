package com.campus.aed.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.aed.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 设备配件实体(电极片/电池)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("device_part")
public class DevicePart extends BaseEntity {

    /** 所属设备ID */
    private Long deviceId;

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
}
