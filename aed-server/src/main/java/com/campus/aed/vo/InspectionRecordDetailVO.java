package com.campus.aed.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 巡检记录详情 VO(在记录基础上附带逐项检查明细)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InspectionRecordDetailVO extends InspectionRecordVO {

    /** 检查明细列表(按明细ID升序) */
    private List<InspectionItemVO> items;

    /**
     * 检查明细 VO
     */
    @Data
    public static class InspectionItemVO {

        /** 检查项名称 */
        private String itemName;

        /** 检查结果: 1-正常 2-异常 */
        private Integer itemResult;

        /** 异常说明 */
        private String remark;
    }
}
