package com.campus.aed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.aed.common.BusinessException;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.PartDTO;
import com.campus.aed.dto.PartQueryDTO;
import com.campus.aed.entity.AedDevice;
import com.campus.aed.entity.DevicePart;
import com.campus.aed.entity.ExpiryWarning;
import com.campus.aed.mapper.AedDeviceMapper;
import com.campus.aed.mapper.DevicePartMapper;
import com.campus.aed.mapper.ExpiryWarningMapper;
import com.campus.aed.service.DevicePartService;
import com.campus.aed.vo.PartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 设备配件服务实现
 */
@Service
@RequiredArgsConstructor
public class DevicePartServiceImpl extends ServiceImpl<DevicePartMapper, DevicePart> implements DevicePartService {

    private final AedDeviceMapper aedDeviceMapper;

    private final ExpiryWarningMapper expiryWarningMapper;

    /** 同设备同类型配件操作的串行化锁: device_part 表无 (device_id, part_type) 唯一索引兜底, 防止并发产生多条"使用中"记录 */
    private final ConcurrentHashMap<String, Object> partLocks = new ConcurrentHashMap<>();

    /** 获取指定设备+配件类型的串行化锁 */
    private Object partLock(Long deviceId, Integer partType) {
        return partLocks.computeIfAbsent(deviceId + "-" + partType, key -> new Object());
    }

    /** 配件分页查询: deviceId/partType/status 过滤, 按创建时间倒序 */
    @Override
    public PageResult<PartVO> pageQuery(PartQueryDTO dto) {
        long pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        long pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        LambdaQueryWrapper<DevicePart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getDeviceId() != null, DevicePart::getDeviceId, dto.getDeviceId())
                .eq(dto.getPartType() != null, DevicePart::getPartType, dto.getPartType())
                .eq(dto.getStatus() != null, DevicePart::getStatus, dto.getStatus())
                .orderByDesc(DevicePart::getCreateTime);
        Page<DevicePart> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<PartVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        fillDeviceInfo(records);
        PageResult<PartVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(records);
        return result;
    }

    /** 某设备配件列表: deviceId/status 过滤, 不分页 */
    @Override
    public List<PartVO> listQuery(PartQueryDTO dto) {
        LambdaQueryWrapper<DevicePart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getDeviceId() != null, DevicePart::getDeviceId, dto.getDeviceId())
                .eq(dto.getStatus() != null, DevicePart::getStatus, dto.getStatus())
                .orderByDesc(DevicePart::getCreateTime);
        List<PartVO> records = this.list(wrapper).stream().map(this::toVO).collect(Collectors.toList());
        fillDeviceInfo(records);
        return records;
    }

    /**
     * 新增配件: 状态固定为使用中(0);
     * 同设备同类型且使用中的旧配件自动置为已更换(1),
     * 同时旧配件关联的未处理预警自动置为已处理(handleDesc=配件已更换)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartVO add(PartDTO dto) {
        // 配件类型校验
        checkPartType(dto.getPartType());
        // 有效期不能早于安装日期
        if (dto.getInstallDate() != null && dto.getExpireDate().isBefore(dto.getInstallDate())) {
            throw new BusinessException("有效期不能早于安装日期");
        }
        // 同设备同类型的新增串行化(加锁后再做首次读, 保证事务快照能看到前一事务已提交的结果),
        // 防止并发新增时"查旧配件→置已更换→插入新配件"链路产生两条"使用中"记录
        synchronized (partLock(dto.getDeviceId(), dto.getPartType())) {
            // 所属设备必须存在
            AedDevice device = aedDeviceMapper.selectById(dto.getDeviceId());
            if (device == null) {
                throw new BusinessException("所属设备不存在");
            }
            // 旧配件自动置为已更换, 并联动关闭其未处理预警
            List<DevicePart> oldParts = this.list(new LambdaQueryWrapper<DevicePart>()
                    .eq(DevicePart::getDeviceId, dto.getDeviceId())
                    .eq(DevicePart::getPartType, dto.getPartType())
                    .eq(DevicePart::getStatus, 0));
            for (DevicePart oldPart : oldParts) {
                oldPart.setStatus(1);
                this.updateById(oldPart);
                // 旧配件关联的未处理预警置为已处理
                ExpiryWarning warningUpdate = new ExpiryWarning();
                warningUpdate.setStatus(1);
                warningUpdate.setHandleDesc("配件已更换");
                warningUpdate.setHandleTime(LocalDateTime.now());
                expiryWarningMapper.update(warningUpdate, new LambdaQueryWrapper<ExpiryWarning>()
                        .eq(ExpiryWarning::getPartId, oldPart.getId())
                        .eq(ExpiryWarning::getStatus, 0));
            }
            // 保存新配件, 状态固定为使用中
            DevicePart part = new DevicePart();
            part.setDeviceId(dto.getDeviceId());
            part.setPartType(dto.getPartType());
            part.setBatchNo(dto.getBatchNo());
            part.setInstallDate(dto.getInstallDate());
            part.setExpireDate(dto.getExpireDate());
            part.setStatus(0);
            part.setRemark(dto.getRemark());
            this.save(part);
            PartVO vo = toVO(part);
            fillDeviceInfo(List.of(vo));
            return vo;
        }
    }

    /** 修改配件 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartVO update(PartDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("配件ID不能为空");
        }
        checkPartType(dto.getPartType());
        DevicePart existing = this.getById(dto.getId());
        if (existing == null) {
            throw new BusinessException("配件不存在");
        }
        // 所属设备必须存在(允许修改设备)
        if (!Objects.equals(dto.getDeviceId(), existing.getDeviceId())) {
            AedDevice device = aedDeviceMapper.selectById(dto.getDeviceId());
            if (device == null) {
                throw new BusinessException("所属设备不存在");
            }
        }
        // 同设备同类型仅允许一个"使用中"配件: 修改设备或配件类型前,
        // 校验目标设备+类型是否已存在其他使用中配件(排除自身), 防止破坏该业务不变量
        Long conflictCount = this.count(new LambdaQueryWrapper<DevicePart>()
                .eq(DevicePart::getDeviceId, dto.getDeviceId())
                .eq(DevicePart::getPartType, dto.getPartType())
                .eq(DevicePart::getStatus, 0)
                .ne(DevicePart::getId, existing.getId()));
        if (conflictCount > 0) {
            throw new BusinessException("同设备同类型已存在使用中配件");
        }
        // 有效期校验: 以更新后的安装日期为准(未传则沿用原值)
        LocalDate effectiveInstallDate = dto.getInstallDate() != null ? dto.getInstallDate() : existing.getInstallDate();
        if (effectiveInstallDate != null && dto.getExpireDate().isBefore(effectiveInstallDate)) {
            throw new BusinessException("有效期不能早于安装日期");
        }
        existing.setDeviceId(dto.getDeviceId());
        existing.setPartType(dto.getPartType());
        existing.setBatchNo(dto.getBatchNo());
        existing.setInstallDate(dto.getInstallDate());
        existing.setExpireDate(dto.getExpireDate());
        existing.setRemark(dto.getRemark());
        this.updateById(existing);
        PartVO vo = toVO(existing);
        fillDeviceInfo(List.of(vo));
        return vo;
    }

    /** 删除配件(逻辑删除) */
    @Override
    public void delete(Long id) {
        DevicePart part = this.getById(id);
        if (part == null) {
            throw new BusinessException("配件不存在");
        }
        this.removeById(id);
    }

    /** 配件类型只能是 1(电极片) 或 2(电池) */
    private void checkPartType(Integer partType) {
        if (partType == null || (partType != 1 && partType != 2)) {
            throw new BusinessException("配件类型只能为1或2");
        }
    }

    /** 实体转 VO(不含设备信息) */
    private PartVO toVO(DevicePart part) {
        PartVO vo = new PartVO();
        vo.setId(part.getId());
        vo.setDeviceId(part.getDeviceId());
        vo.setPartType(part.getPartType());
        vo.setBatchNo(part.getBatchNo());
        vo.setInstallDate(part.getInstallDate());
        vo.setExpireDate(part.getExpireDate());
        vo.setStatus(part.getStatus());
        vo.setRemark(part.getRemark());
        vo.setCreateTime(part.getCreateTime());
        vo.setUpdateTime(part.getUpdateTime());
        return vo;
    }

    /**
     * 批量填充 deviceCode/deviceName;
     * 设备已逻辑删除时查不到, 对应字段保持为 null
     */
    private void fillDeviceInfo(List<PartVO> records) {
        if (records.isEmpty()) {
            return;
        }
        Set<Long> deviceIds = records.stream()
                .map(PartVO::getDeviceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (deviceIds.isEmpty()) {
            return;
        }
        Map<Long, AedDevice> deviceMap = aedDeviceMapper.selectBatchIds(deviceIds).stream()
                .collect(Collectors.toMap(AedDevice::getId, Function.identity()));
        records.forEach(vo -> {
            AedDevice device = deviceMap.get(vo.getDeviceId());
            if (device != null) {
                vo.setDeviceCode(device.getDeviceCode());
                vo.setDeviceName(device.getDeviceName());
            }
        });
    }
}
