package com.campus.aed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.aed.common.BusinessException;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.CompleteRepairDTO;
import com.campus.aed.dto.RejectRepairDTO;
import com.campus.aed.dto.RepairQueryDTO;
import com.campus.aed.dto.ReportRepairDTO;
import com.campus.aed.entity.AedDevice;
import com.campus.aed.entity.RepairOrder;
import com.campus.aed.entity.SysUser;
import com.campus.aed.mapper.AedDeviceMapper;
import com.campus.aed.mapper.RepairOrderMapper;
import com.campus.aed.mapper.SysUserMapper;
import com.campus.aed.service.RepairOrderService;
import com.campus.aed.util.UserContext;
import com.campus.aed.vo.RepairOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 维修工单服务实现
 */
@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl extends ServiceImpl<RepairOrderMapper, RepairOrder>
        implements RepairOrderService {

    private final AedDeviceMapper aedDeviceMapper;

    private final SysUserMapper sysUserMapper;

    @Override
    public PageResult<RepairOrderVO> pageQuery(RepairQueryDTO dto) {
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<>();
        // 按状态过滤(选填)
        wrapper.eq(dto.getStatus() != null, RepairOrder::getStatus, dto.getStatus());
        // 按设备过滤(选填)
        wrapper.eq(dto.getDeviceId() != null, RepairOrder::getDeviceId, dto.getDeviceId());
        // 按创建时间倒序, 同时间再按ID倒序保证排序稳定
        wrapper.orderByDesc(RepairOrder::getCreateTime).orderByDesc(RepairOrder::getId);
        Page<RepairOrder> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<RepairOrderVO> records = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        // 批量填充设备编号/型号与报修人/处理人姓名, 避免逐条查询
        fillNames(records);
        PageResult<RepairOrderVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public RepairOrderVO report(ReportRepairDTO dto) {
        // 报修前校验设备是否存在(已逻辑删除的设备视为不存在)
        if (aedDeviceMapper.selectById(dto.getDeviceId()) == null) {
            throw new BusinessException(400, "设备不存在, 无法报修");
        }
        RepairOrder order = new RepairOrder();
        order.setOrderNo(generateOrderNo());
        order.setDeviceId(dto.getDeviceId());
        // 手动报修来源固定为 2
        order.setReportType(2);
        // 报修人为当前登录用户
        order.setReporterId(UserContext.getUserId());
        order.setFaultDesc(dto.getFaultDesc());
        order.setFaultPhoto(dto.getFaultPhoto());
        // 新工单状态为待处理
        order.setStatus(0);
        this.save(order);
        return detail(order.getId());
    }

    @Override
    public RepairOrderVO detail(Long id) {
        RepairOrder order = requireOrder(id);
        RepairOrderVO vo = toVO(order);
        fillNames(List.of(vo));
        return vo;
    }

    @Override
    public void accept(Long id) {
        // 先校验工单存在(逻辑删除已自动过滤), 统一提示"维修工单不存在"
        requireOrder(id);
        // 仅待处理(0)可受理; 条件更新保证并发下状态判断与更新原子性
        LambdaUpdateWrapper<RepairOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RepairOrder::getId, id)
                .eq(RepairOrder::getStatus, 0)
                .set(RepairOrder::getStatus, 1)
                .set(RepairOrder::getHandlerId, UserContext.getUserId());
        if (!this.update(updateWrapper)) {
            throw new BusinessException("该工单当前状态不可受理");
        }
    }

    @Override
    public void complete(Long id, CompleteRepairDTO dto) {
        requireOrder(id);
        // 仅维修中(1)可完成; 维修说明/费用/完成时间一并写入
        LambdaUpdateWrapper<RepairOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RepairOrder::getId, id)
                .eq(RepairOrder::getStatus, 1)
                .set(RepairOrder::getStatus, 2)
                .set(RepairOrder::getRepairDesc, dto.getRepairDesc())
                .set(RepairOrder::getRepairCost, dto.getRepairCost())
                .set(RepairOrder::getRepairTime, LocalDateTime.now());
        if (!this.update(updateWrapper)) {
            throw new BusinessException("该工单当前状态不可完成");
        }
    }

    @Override
    public void reject(Long id, RejectRepairDTO dto) {
        requireOrder(id);
        // 仅待处理(0)可驳回
        LambdaUpdateWrapper<RepairOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RepairOrder::getId, id)
                .eq(RepairOrder::getStatus, 0)
                .set(RepairOrder::getStatus, 3)
                .set(RepairOrder::getRejectReason, dto.getRejectReason());
        if (!this.update(updateWrapper)) {
            throw new BusinessException("该工单当前状态不可驳回");
        }
    }

    /**
     * 校验工单存在, 不存在统一抛"维修工单不存在"
     */
    private RepairOrder requireOrder(Long id) {
        RepairOrder order = this.getById(id);
        if (order == null) {
            throw new BusinessException("维修工单不存在");
        }
        return order;
    }

    /**
     * 生成工单编号: WX + yyyyMMddHHmmss + 3位随机数字
     */
    private String generateOrderNo() {
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int random = ThreadLocalRandom.current().nextInt(100, 1000);
        return "WX" + time + random;
    }

    /**
     * 实体转 VO(冗余姓名字段由 fillNames 统一填充)
     */
    private RepairOrderVO toVO(RepairOrder order) {
        RepairOrderVO vo = new RepairOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setDeviceId(order.getDeviceId());
        vo.setReportType(order.getReportType());
        vo.setReporterId(order.getReporterId());
        vo.setFaultDesc(order.getFaultDesc());
        vo.setFaultPhoto(order.getFaultPhoto());
        vo.setStatus(order.getStatus());
        vo.setHandlerId(order.getHandlerId());
        vo.setRepairDesc(order.getRepairDesc());
        vo.setRepairCost(order.getRepairCost());
        vo.setRepairTime(order.getRepairTime());
        vo.setRejectReason(order.getRejectReason());
        vo.setCreateTime(order.getCreateTime());
        vo.setUpdateTime(order.getUpdateTime());
        return vo;
    }

    /**
     * 批量填充设备编号/设备型号/报修人姓名/处理人姓名, 关联数据缺失时保持 null
     */
    private void fillNames(List<RepairOrderVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        // 收集需要查询的设备ID与用户ID
        Set<Long> deviceIds = records.stream()
                .map(RepairOrderVO::getDeviceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> userIds = new HashSet<>();
        for (RepairOrderVO vo : records) {
            if (vo.getReporterId() != null) {
                userIds.add(vo.getReporterId());
            }
            if (vo.getHandlerId() != null) {
                userIds.add(vo.getHandlerId());
            }
        }
        // 批量查询设备与用户, 组装 ID -> 实体 映射
        Map<Long, AedDevice> deviceMap = deviceIds.isEmpty() ? Map.of()
                : aedDeviceMapper.selectBatchIds(deviceIds).stream()
                        .collect(Collectors.toMap(AedDevice::getId, d -> d));
        Map<Long, SysUser> userMap = userIds.isEmpty() ? Map.of()
                : sysUserMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u));
        // 回填冗余字段
        for (RepairOrderVO vo : records) {
            AedDevice device = deviceMap.get(vo.getDeviceId());
            if (device != null) {
                vo.setDeviceCode(device.getDeviceCode());
                vo.setDeviceName(device.getDeviceName());
            }
            SysUser reporter = userMap.get(vo.getReporterId());
            if (reporter != null) {
                vo.setReporterName(reporter.getRealName());
            }
            SysUser handler = userMap.get(vo.getHandlerId());
            if (handler != null) {
                vo.setHandlerName(handler.getRealName());
            }
        }
    }
}
