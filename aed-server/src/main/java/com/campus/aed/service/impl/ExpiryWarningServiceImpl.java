package com.campus.aed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.aed.common.BusinessException;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.HandleWarningDTO;
import com.campus.aed.dto.WarningQueryDTO;
import com.campus.aed.entity.AedDevice;
import com.campus.aed.entity.DevicePart;
import com.campus.aed.entity.ExpiryWarning;
import com.campus.aed.mapper.AedDeviceMapper;
import com.campus.aed.mapper.DevicePartMapper;
import com.campus.aed.mapper.ExpiryWarningMapper;
import com.campus.aed.service.ExpiryWarningService;
import com.campus.aed.vo.WarningVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 到期预警服务实现
 */
@Service
@RequiredArgsConstructor
public class ExpiryWarningServiceImpl extends ServiceImpl<ExpiryWarningMapper, ExpiryWarning>
        implements ExpiryWarningService {

    /** 临期阈值: 30天内到期生成临期预警 */
    private static final int EXPIRING_DAYS = 30;

    /** 配件已更换/已删除时预警自动关闭的处理说明 */
    private static final String AUTO_HANDLE_DESC = "配件已更换";

    private final DevicePartMapper devicePartMapper;

    private final AedDeviceMapper aedDeviceMapper;

    @Override
    public PageResult<WarningVO> pageQuery(WarningQueryDTO dto) {
        LambdaQueryWrapper<ExpiryWarning> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getStatus() != null, ExpiryWarning::getStatus, dto.getStatus())
                .eq(dto.getWarningType() != null, ExpiryWarning::getWarningType, dto.getWarningType())
                .orderByDesc(ExpiryWarning::getWarningDate)
                .orderByDesc(ExpiryWarning::getId);
        Page<ExpiryWarning> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        PageResult<WarningVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(toVOList(page.getRecords()));
        return result;
    }

    @Override
    public WarningVO handle(Long id, HandleWarningDTO dto) {
        ExpiryWarning warning = this.getById(id);
        if (warning == null) {
            throw new BusinessException("预警记录不存在");
        }
        // 仅未处理(status=0)的预警可处理, 防止重复处理
        if (!Integer.valueOf(0).equals(warning.getStatus())) {
            throw new BusinessException("该预警已处理, 不能重复处理");
        }
        warning.setStatus(1);
        warning.setHandleDesc(dto.getHandleDesc());
        warning.setHandleTime(LocalDateTime.now());
        this.updateById(warning);
        return toVO(warning);
    }

    /**
     * 全量扫描生成临期/过期预警。
     * synchronized 保证同一 JVM 内并发调用(如任务超时 misfire 补跑、人工触发)串行执行,
     * 避免"先查后插"去重在并发下失效而重复生成预警(定时任务侧另有 @DisallowConcurrentExecution 兜底)。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void scanExpiry() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // ① 查询所有"使用中"的配件
        List<DevicePart> usingParts = devicePartMapper.selectList(
                new LambdaQueryWrapper<DevicePart>().eq(DevicePart::getStatus, 0));

        // ② 查询所有未处理预警, 用于去重、状态同步与后续自动关闭
        List<ExpiryWarning> pendingWarnings = this.list(
                new LambdaQueryWrapper<ExpiryWarning>().eq(ExpiryWarning::getStatus, 0));
        Map<Long, ExpiryWarning> pendingByPartId = pendingWarnings.stream()
                .filter(w -> w.getPartId() != null)
                .collect(Collectors.toMap(ExpiryWarning::getPartId, Function.identity(), (a, b) -> a));

        // ③ 逐个配件计算剩余天数, 生成临期/过期预警
        List<ExpiryWarning> toInsert = new ArrayList<>();
        List<ExpiryWarning> toUpdate = new ArrayList<>();
        for (DevicePart part : usingParts) {
            if (part.getExpireDate() == null) {
                continue;
            }
            long days = ChronoUnit.DAYS.between(today, part.getExpireDate());
            Integer warningType;
            if (days < 0) {
                warningType = 2; // 已过期
            } else if (days <= EXPIRING_DAYS) {
                warningType = 1; // 30天内到期为临期
            } else {
                continue; // 尚未到期, 不生成预警
            }
            // 同一配件已存在未处理预警则不重复生成, 但需同步最新类型与剩余天数
            // (如配件已过期, 临期预警应升级为类型2且剩余天数为负数, 否则长期显示为临期)
            ExpiryWarning existing = pendingByPartId.get(part.getId());
            if (existing != null) {
                if (!Objects.equals(existing.getWarningType(), warningType)
                        || !Objects.equals(existing.getDaysRemaining(), (int) days)) {
                    existing.setWarningType(warningType);
                    existing.setDaysRemaining((int) days);
                    toUpdate.add(existing);
                }
                continue;
            }
            ExpiryWarning warning = new ExpiryWarning();
            warning.setPartId(part.getId());
            warning.setDeviceId(part.getDeviceId());
            warning.setWarningType(warningType);
            warning.setDaysRemaining((int) days);
            warning.setWarningDate(today);
            warning.setStatus(0);
            toInsert.add(warning);
        }
        if (!toInsert.isEmpty()) {
            this.saveBatch(toInsert);
        }
        if (!toUpdate.isEmpty()) {
            this.updateBatchById(toUpdate);
        }

        // ④ 配件已更换(status=1)或已不存在(含逻辑删除)的未处理预警自动置为已处理
        Set<Long> pendingPartIds = pendingWarnings.stream()
                .map(ExpiryWarning::getPartId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, DevicePart> partMap = pendingPartIds.isEmpty() ? Collections.emptyMap()
                : devicePartMapper.selectBatchIds(pendingPartIds).stream()
                        .collect(Collectors.toMap(DevicePart::getId, Function.identity(), (a, b) -> a));
        List<Long> autoCloseIds = new ArrayList<>();
        for (ExpiryWarning pending : pendingWarnings) {
            DevicePart part = partMap.get(pending.getPartId());
            if (part == null || Integer.valueOf(1).equals(part.getStatus())) {
                autoCloseIds.add(pending.getId());
            }
        }
        if (!autoCloseIds.isEmpty()) {
            LambdaUpdateWrapper<ExpiryWarning> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(ExpiryWarning::getId, autoCloseIds)
                    .set(ExpiryWarning::getStatus, 1)
                    .set(ExpiryWarning::getHandleDesc, AUTO_HANDLE_DESC)
                    .set(ExpiryWarning::getHandleTime, now);
            this.update(updateWrapper);
        }
    }

    /**
     * 批量组装预警视图(批量查询配件与设备, 避免逐条查询)
     */
    private List<WarningVO> toVOList(List<ExpiryWarning> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> partIds = records.stream()
                .map(ExpiryWarning::getPartId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, DevicePart> partMap = partIds.isEmpty() ? Collections.emptyMap()
                : devicePartMapper.selectBatchIds(partIds).stream()
                        .collect(Collectors.toMap(DevicePart::getId, Function.identity(), (a, b) -> a));
        Set<Long> deviceIds = records.stream()
                .map(ExpiryWarning::getDeviceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, AedDevice> deviceMap = deviceIds.isEmpty() ? Collections.emptyMap()
                : aedDeviceMapper.selectBatchIds(deviceIds).stream()
                        .collect(Collectors.toMap(AedDevice::getId, Function.identity(), (a, b) -> a));
        return records.stream()
                .map(record -> toVO(record, partMap, deviceMap))
                .collect(Collectors.toList());
    }

    /** 单条预警组装视图 */
    private WarningVO toVO(ExpiryWarning warning) {
        Map<Long, DevicePart> partMap = warning.getPartId() == null ? Collections.emptyMap()
                : devicePartMapper.selectBatchIds(List.of(warning.getPartId())).stream()
                        .collect(Collectors.toMap(DevicePart::getId, Function.identity(), (a, b) -> a));
        Map<Long, AedDevice> deviceMap = warning.getDeviceId() == null ? Collections.emptyMap()
                : aedDeviceMapper.selectBatchIds(List.of(warning.getDeviceId())).stream()
                        .collect(Collectors.toMap(AedDevice::getId, Function.identity(), (a, b) -> a));
        return toVO(warning, partMap, deviceMap);
    }

    /** 根据已加载的配件/设备映射组装视图(设备与配件可能已删除, 此时对应字段为 null) */
    private WarningVO toVO(ExpiryWarning warning, Map<Long, DevicePart> partMap, Map<Long, AedDevice> deviceMap) {
        WarningVO vo = new WarningVO();
        vo.setId(warning.getId());
        vo.setPartId(warning.getPartId());
        vo.setDeviceId(warning.getDeviceId());
        DevicePart part = warning.getPartId() == null ? null : partMap.get(warning.getPartId());
        if (part != null) {
            vo.setPartType(part.getPartType());
            vo.setExpireDate(part.getExpireDate());
        }
        AedDevice device = warning.getDeviceId() == null ? null : deviceMap.get(warning.getDeviceId());
        if (device != null) {
            vo.setDeviceCode(device.getDeviceCode());
            vo.setDeviceName(device.getDeviceName());
        }
        vo.setWarningType(warning.getWarningType());
        vo.setDaysRemaining(warning.getDaysRemaining());
        vo.setWarningDate(warning.getWarningDate());
        vo.setStatus(warning.getStatus());
        vo.setHandleDesc(warning.getHandleDesc());
        vo.setHandleTime(warning.getHandleTime());
        return vo;
    }
}
