package com.campus.aed.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.CompleteRepairDTO;
import com.campus.aed.dto.RejectRepairDTO;
import com.campus.aed.dto.RepairQueryDTO;
import com.campus.aed.dto.ReportRepairDTO;
import com.campus.aed.entity.RepairOrder;
import com.campus.aed.vo.RepairOrderVO;

/**
 * 维修工单服务接口
 */
public interface RepairOrderService extends IService<RepairOrder> {

    /**
     * 工单分页查询(status/deviceId 过滤, 按创建时间倒序)
     */
    PageResult<RepairOrderVO> pageQuery(RepairQueryDTO dto);

    /**
     * 手动报修(所有登录角色), 生成待处理工单
     */
    RepairOrderVO report(ReportRepairDTO dto);

    /**
     * 工单详情
     */
    RepairOrderVO detail(Long id);

    /**
     * 受理工单: 待处理(0) -> 维修中(1), 记录处理人
     */
    void accept(Long id);

    /**
     * 完成工单: 维修中(1) -> 已完成(2), 记录维修说明/费用/完成时间
     */
    void complete(Long id, CompleteRepairDTO dto);

    /**
     * 驳回工单: 待处理(0) -> 已驳回(3), 记录驳回原因
     */
    void reject(Long id, RejectRepairDTO dto);
}
