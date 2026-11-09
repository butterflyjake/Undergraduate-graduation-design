package com.campus.aed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.aed.common.BusinessException;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.UsageDTO;
import com.campus.aed.dto.UsageQueryDTO;
import com.campus.aed.entity.AedDevice;
import com.campus.aed.entity.SysUser;
import com.campus.aed.entity.UsageRecord;
import com.campus.aed.mapper.AedDeviceMapper;
import com.campus.aed.mapper.SysUserMapper;
import com.campus.aed.mapper.UsageRecordMapper;
import com.campus.aed.service.UsageRecordService;
import com.campus.aed.util.UserContext;
import com.campus.aed.vo.UsageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AED使用记录 Service 实现
 */
@Service
@RequiredArgsConstructor
public class UsageRecordServiceImpl extends ServiceImpl<UsageRecordMapper, UsageRecord>
        implements UsageRecordService {

    private final AedDeviceMapper aedDeviceMapper;

    private final SysUserMapper sysUserMapper;

    @Override
    public PageResult<UsageVO> pageQuery(UsageQueryDTO dto) {
        // 查询条件: 支持按设备过滤
        LambdaQueryWrapper<UsageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getDeviceId() != null, UsageRecord::getDeviceId, dto.getDeviceId());
        // 按使用发生时间倒序, 同时间再按ID倒序保证排序稳定
        wrapper.orderByDesc(UsageRecord::getOccurTime).orderByDesc(UsageRecord::getId);
        Page<UsageRecord> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<UsageRecord> records = page.getRecords();
        // 批量加载设备与登记人信息, 避免逐条查询
        Map<Long, AedDevice> deviceMap = loadDeviceMap(records);
        Map<Long, SysUser> userMap = loadUserMap(records);
        List<UsageVO> voList = records.stream()
                .map(record -> toVO(record, deviceMap, userMap))
                .collect(Collectors.toList());
        PageResult<UsageVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    public UsageVO add(UsageDTO dto) {
        // 登记前校验设备是否存在(已逻辑删除的设备视为不存在)
        if (aedDeviceMapper.selectById(dto.getDeviceId()) == null) {
            throw new BusinessException("设备不存在, 无法登记使用记录");
        }
        UsageRecord record = new UsageRecord();
        record.setDeviceId(dto.getDeviceId());
        record.setOccurTime(dto.getOccurTime());
        record.setLocationDesc(dto.getLocationDesc());
        record.setPatientDesc(dto.getPatientDesc());
        record.setResultDesc(dto.getResultDesc());
        record.setRemark(dto.getRemark());
        // 登记人固定为当前登录用户, 不信任前端传入
        record.setRecorderId(UserContext.getUserId());
        this.save(record);
        // 返回新增记录的完整详情
        return getDetail(record.getId());
    }

    @Override
    public UsageVO getDetail(Long id) {
        UsageRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException("使用记录不存在");
        }
        return toVO(record, loadDeviceMap(List.of(record)), loadUserMap(List.of(record)));
    }

    /** 实体转 VO, 设备/登记人缺失时对应字段置 null */
    private UsageVO toVO(UsageRecord record, Map<Long, AedDevice> deviceMap, Map<Long, SysUser> userMap) {
        UsageVO vo = new UsageVO();
        vo.setId(record.getId());
        vo.setDeviceId(record.getDeviceId());
        AedDevice device = deviceMap.get(record.getDeviceId());
        vo.setDeviceCode(device == null ? null : device.getDeviceCode());
        vo.setDeviceName(device == null ? null : device.getDeviceName());
        vo.setOccurTime(record.getOccurTime());
        vo.setLocationDesc(record.getLocationDesc());
        vo.setPatientDesc(record.getPatientDesc());
        vo.setResultDesc(record.getResultDesc());
        vo.setRecorderId(record.getRecorderId());
        SysUser recorder = userMap.get(record.getRecorderId());
        vo.setRecorderName(recorder == null ? null : recorder.getRealName());
        vo.setRemark(record.getRemark());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }

    /** 批量加载设备, 组装 id -> 设备 映射 */
    private Map<Long, AedDevice> loadDeviceMap(List<UsageRecord> records) {
        List<Long> deviceIds = records.stream()
                .map(UsageRecord::getDeviceId)
                .distinct()
                .collect(Collectors.toList());
        if (deviceIds.isEmpty()) {
            return Map.of();
        }
        return aedDeviceMapper.selectBatchIds(deviceIds).stream()
                .collect(Collectors.toMap(AedDevice::getId, Function.identity()));
    }

    /** 批量加载登记人, 组装 id -> 用户 映射 */
    private Map<Long, SysUser> loadUserMap(List<UsageRecord> records) {
        List<Long> userIds = records.stream()
                .map(UsageRecord::getRecorderId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
    }
}
