package com.campus.aed.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.InspectionRecordQueryDTO;
import com.campus.aed.dto.SubmitInspectionDTO;
import com.campus.aed.entity.InspectionRecord;
import com.campus.aed.vo.InspectionRecordDetailVO;
import com.campus.aed.vo.InspectionRecordVO;

/**
 * 巡检记录服务接口
 */
public interface InspectionRecordService extends IService<InspectionRecord> {

    /**
     * 提交巡检记录: 生成记录与明细、完成任务; 存在异常项时自动生成维修工单
     */
    void submit(SubmitInspectionDTO dto);

    /**
     * 巡检记录分页查询(inspector 角色仅查本人记录, 按巡检时间倒序)
     */
    PageResult<InspectionRecordVO> pageQuery(InspectionRecordQueryDTO dto);

    /**
     * 巡检记录详情(含逐项检查明细)
     */
    InspectionRecordDetailVO getDetail(Long id);
}
