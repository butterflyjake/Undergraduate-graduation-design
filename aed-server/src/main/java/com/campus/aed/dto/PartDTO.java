package com.campus.aed.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 配件新增/修改请求参数
 */
@Data
public class PartDTO {

    /** 配件ID(修改时必填, 新增时不传) */
    private Long id;

    /** 所属设备ID */
    @NotNull(message = "所属设备不能为空")
    private Long deviceId;

    /** 配件类型: 1-电极片 2-电池 */
    @NotNull(message = "配件类型不能为空")
    @Min(value = 1, message = "配件类型只能为1或2")
    @Max(value = 2, message = "配件类型只能为1或2")
    private Integer partType;

    /** 批次号 */
    private String batchNo;

    /** 安装日期 */
    private LocalDate installDate;

    /** 有效期至 */
    @NotNull(message = "有效期不能为空")
    private LocalDate expireDate;

    /** 备注 */
    private String remark;
}
