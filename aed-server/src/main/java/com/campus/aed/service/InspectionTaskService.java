package com.campus.aed.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.InspectionTaskQueryDTO;
import com.campus.aed.entity.InspectionTask;
import com.campus.aed.vo.InspectionTaskVO;

import java.time.LocalDate;

/**
 * 巡检任务服务接口
 */
public interface InspectionTaskService extends IService<InspectionTask> {

    /**
     * 生成巡检任务(定时任务与手动生成共用)
     * ① 将计划日期早于 planDate 的待巡检任务标记为已逾期;
     * ② 为所有"在用"设备生成当天巡检任务;
     * ③ 任务分配给当前待巡检任务数量最少的巡检人员(负载均衡);
     * ④ 系统无巡检人员时抛出业务异常
     *
     * @param planDate 计划巡检日期
     */
    int generateTasks(LocalDate planDate);

    /**
     * 巡检任务分页查询(inspector 角色仅查本人任务)
     */
    PageResult<InspectionTaskVO> pageQuery(InspectionTaskQueryDTO dto);

    /**
     * 巡检任务详情
     */
    InspectionTaskVO getDetail(Long id);
}
