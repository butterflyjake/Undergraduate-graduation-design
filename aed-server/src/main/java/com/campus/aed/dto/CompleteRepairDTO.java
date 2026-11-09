package com.campus.aed.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 完成维修请求 DTO
 */
@Data
public class CompleteRepairDTO {

    /** 维修说明(必填) */
    @NotBlank(message = "维修说明不能为空")
    @Size(max = 500, message = "维修说明不能超过500个字符")
    private String repairDesc;

    /** 维修费用(元), 选填, 不允许为负数 */
    @DecimalMin(value = "0", message = "维修费用不能为负数")
    private BigDecimal repairCost;
}
