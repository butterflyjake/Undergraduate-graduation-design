package com.campus.aed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.aed.common.BusinessException;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.DeviceDTO;
import com.campus.aed.dto.DeviceQueryDTO;
import com.campus.aed.entity.AedDevice;
import com.campus.aed.entity.Building;
import com.campus.aed.entity.InspectionTask;
import com.campus.aed.entity.RepairOrder;
import com.campus.aed.mapper.AedDeviceMapper;
import com.campus.aed.mapper.BuildingMapper;
import com.campus.aed.mapper.InspectionTaskMapper;
import com.campus.aed.mapper.RepairOrderMapper;
import com.campus.aed.service.AedDeviceService;
import com.campus.aed.vo.DeviceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 设备台账服务实现
 */
@Service
@RequiredArgsConstructor
public class AedDeviceServiceImpl extends ServiceImpl<AedDeviceMapper, AedDevice> implements AedDeviceService {

    /** 维修工单状态: 维修中 */
    private static final int REPAIR_STATUS_REPAIRING = 1;

    /** 巡检任务状态: 待巡检 */
    private static final int TASK_STATUS_PENDING = 0;

    private final BuildingMapper buildingMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final InspectionTaskMapper inspectionTaskMapper;

    @Override
    public PageResult<DeviceVO> pageQuery(DeviceQueryDTO dto) {
        LambdaQueryWrapper<AedDevice> wrapper = new LambdaQueryWrapper<>();
        // 关键字模糊匹配设备编号或设备名称(内嵌分组, 避免影响其他过滤条件)
        wrapper.and(StringUtils.hasText(dto.getKeyword()), w -> w
                .like(AedDevice::getDeviceCode, dto.getKeyword())
                .or()
                .like(AedDevice::getDeviceName, dto.getKeyword()));
        // 楼宇过滤
        wrapper.eq(dto.getBuildingId() != null, AedDevice::getBuildingId, dto.getBuildingId());
        // 状态过滤
        wrapper.eq(dto.getStatus() != null, AedDevice::getStatus, dto.getStatus());
        // 按创建时间倒序
        wrapper.orderByDesc(AedDevice::getCreateTime);

        Page<AedDevice> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);

        // 批量查询楼宇名称, 避免逐条查询
        Map<Long, String> buildingNameMap = loadBuildingNameMap(page.getRecords());
        List<DeviceVO> records = page.getRecords().stream()
                .map(device -> toVO(device, buildingNameMap))
                .collect(Collectors.toList());

        PageResult<DeviceVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public List<AedDevice> listAll() {
        // 不分页, 按设备编号升序
        return this.list(new LambdaQueryWrapper<AedDevice>().orderByAsc(AedDevice::getDeviceCode));
    }

    @Override
    public DeviceVO getDetail(Long id) {
        AedDevice device = this.getById(id);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        return toVO(device, loadBuildingNameMap(Collections.singletonList(device)));
    }

    @Override
    public DeviceVO create(DeviceDTO dto) {
        // 设备编号唯一校验
        checkDeviceCodeUnique(dto.getDeviceCode(), null);

        AedDevice device = new AedDevice();
        BeanUtils.copyProperties(dto, device);
        // 强制自增主键, 防止传入 id 干扰
        device.setId(null);
        try {
            this.save(device);
        } catch (DuplicateKeyException e) {
            // 并发创建同编号设备时校验可能被绕过, 撞唯一索引后转成友好提示而非 500
            throw new BusinessException("设备编号已存在");
        }
        return toVO(device, loadBuildingNameMap(Collections.singletonList(device)));
    }

    @Override
    public DeviceVO update(DeviceDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("设备ID不能为空");
        }
        AedDevice device = this.getById(dto.getId());
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        // 设备编号唯一校验(排除自身)
        checkDeviceCodeUnique(dto.getDeviceCode(), dto.getId());

        BeanUtils.copyProperties(dto, device);
        try {
            this.updateById(device);
        } catch (DuplicateKeyException e) {
            // 并发修改设备编号撞唯一索引时转成友好提示而非 500
            throw new BusinessException("设备编号已存在");
        }
        return toVO(device, loadBuildingNameMap(Collections.singletonList(device)));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        // 服务层二次校验, 防止绕过 DTO 校验直接调用
        if (id == null) {
            throw new BusinessException("设备ID不能为空");
        }
        if (status == null || status < 0 || status > 3) {
            throw new BusinessException("状态值不合法, 必须在0-3之间");
        }
        AedDevice device = this.getById(id);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        device.setStatus(status);
        this.updateById(device);
    }

    @Override
    public void delete(Long id) {
        AedDevice device = this.getById(id);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }
        // 存在维修中的维修工单时拒绝删除
        Long repairingCount = repairOrderMapper.selectCount(new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getDeviceId, id)
                .eq(RepairOrder::getStatus, REPAIR_STATUS_REPAIRING));
        if (repairingCount != null && repairingCount > 0) {
            throw new BusinessException("该设备存在维修中的维修工单, 无法删除");
        }
        // 存在待巡检的巡检任务时拒绝删除
        Long pendingTaskCount = inspectionTaskMapper.selectCount(new LambdaQueryWrapper<InspectionTask>()
                .eq(InspectionTask::getDeviceId, id)
                .eq(InspectionTask::getStatus, TASK_STATUS_PENDING));
        if (pendingTaskCount != null && pendingTaskCount > 0) {
            throw new BusinessException("该设备存在待巡检的巡检任务, 无法删除");
        }
        // 逻辑删除全局自动生效
        this.removeById(id);
    }

    /**
     * 校验设备编号唯一; excludeId 不为空时排除自身(修改场景)。
     * 注意: 已逻辑删除的记录仍占用数据库唯一索引 uk_device_code, 普通 count 查询会被
     * @TableLogic 自动过滤而误判通过, 随后 INSERT/UPDATE 撞唯一索引报 500,
     * 因此这里必须连已删除记录一起统计(见 AedDeviceMapper.countByDeviceCodeIncludingDeleted)
     */
    private void checkDeviceCodeUnique(String deviceCode, Long excludeId) {
        Long existCount = baseMapper.countByDeviceCodeIncludingDeleted(deviceCode, excludeId);
        if (existCount != null && existCount > 0) {
            throw new BusinessException("设备编号已存在");
        }
    }

    /**
     * 实体转 VO; buildingId 为空或楼宇不存在时 buildingName 为 null
     */
    private DeviceVO toVO(AedDevice device, Map<Long, String> buildingNameMap) {
        DeviceVO vo = new DeviceVO();
        BeanUtils.copyProperties(device, vo);
        if (device.getBuildingId() != null) {
            vo.setBuildingName(buildingNameMap.get(device.getBuildingId()));
        }
        return vo;
    }

    /**
     * 批量查询设备关联的楼宇名称, 返回 buildingId -> buildingName 映射
     */
    private Map<Long, String> loadBuildingNameMap(List<AedDevice> devices) {
        List<Long> buildingIds = devices.stream()
                .map(AedDevice::getBuildingId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (buildingIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return buildingMapper.selectList(new LambdaQueryWrapper<Building>()
                        .select(Building::getId, Building::getBuildingName)
                        .in(Building::getId, buildingIds))
                .stream()
                .collect(Collectors.toMap(Building::getId, Building::getBuildingName));
    }
}
