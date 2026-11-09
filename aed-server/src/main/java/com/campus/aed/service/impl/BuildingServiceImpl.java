package com.campus.aed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.aed.common.BusinessException;
import com.campus.aed.dto.BuildingDTO;
import com.campus.aed.entity.AedDevice;
import com.campus.aed.entity.Building;
import com.campus.aed.mapper.AedDeviceMapper;
import com.campus.aed.mapper.BuildingMapper;
import com.campus.aed.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 楼宇管理 Service 实现
 */
@Service
@RequiredArgsConstructor
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, Building> implements BuildingService {

    private final AedDeviceMapper aedDeviceMapper;

    @Override
    public List<Building> listAll() {
        // 按id升序返回全部楼宇(逻辑删除自动过滤)
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Building::getId);
        return this.list(wrapper);
    }

    @Override
    public void create(BuildingDTO dto) {
        // 新增时校验楼宇名称唯一(已删除的不参与校验)
        long count = this.count(new LambdaQueryWrapper<Building>()
                .eq(Building::getBuildingName, dto.getBuildingName()));
        if (count > 0) {
            throw new BusinessException("楼宇名称已存在");
        }
        Building building = new Building();
        BeanUtils.copyProperties(dto, building);
        // 新增时主键由数据库自增生成, 防止前端传入的id被写入
        building.setId(null);
        this.save(building);
    }

    @Override
    public void update(BuildingDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("楼宇ID不能为空");
        }
        Building exist = this.getById(dto.getId());
        if (exist == null) {
            throw new BusinessException("楼宇不存在");
        }
        // 修改时校验楼宇名称唯一(排除自身)
        long count = this.count(new LambdaQueryWrapper<Building>()
                .eq(Building::getBuildingName, dto.getBuildingName())
                .ne(Building::getId, dto.getId()));
        if (count > 0) {
            throw new BusinessException("楼宇名称已存在");
        }
        Building building = new Building();
        BeanUtils.copyProperties(dto, building);
        this.updateById(building);
    }

    @Override
    public void delete(Long id) {
        // 该楼宇下存在设备时禁止删除
        Long deviceCount = aedDeviceMapper.selectCount(new LambdaQueryWrapper<AedDevice>()
                .eq(AedDevice::getBuildingId, id));
        if (deviceCount != null && deviceCount > 0) {
            throw new BusinessException("该楼宇下存在设备, 无法删除");
        }
        this.removeById(id);
    }
}
