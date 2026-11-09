package com.campus.aed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.aed.common.BusinessException;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.InspectionTaskQueryDTO;
import com.campus.aed.entity.AedDevice;
import com.campus.aed.entity.InspectionTask;
import com.campus.aed.entity.SysRole;
import com.campus.aed.entity.SysUser;
import com.campus.aed.mapper.AedDeviceMapper;
import com.campus.aed.mapper.InspectionTaskMapper;
import com.campus.aed.mapper.SysRoleMapper;
import com.campus.aed.mapper.SysUserMapper;
import com.campus.aed.service.InspectionTaskService;
import com.campus.aed.util.UserContext;
import com.campus.aed.vo.InspectionTaskVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 巡检任务服务实现
 */
@Service
@RequiredArgsConstructor
public class InspectionTaskServiceImpl extends ServiceImpl<InspectionTaskMapper, InspectionTask> implements InspectionTaskService {

    private final AedDeviceMapper aedDeviceMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysUserMapper sysUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateTasks(LocalDate planDate) {
        // ① 将计划日期早于今天且仍处于待巡检状态的任务标记为已逾期
        // (以今天为界, 避免为未来日期预生成任务时把今日有效任务误置为已逾期)
        InspectionTask overdueUpdate = new InspectionTask();
        overdueUpdate.setStatus(2);
        this.update(overdueUpdate, new LambdaQueryWrapper<InspectionTask>()
                .eq(InspectionTask::getStatus, 0)
                .lt(InspectionTask::getPlanDate, LocalDate.now()));

        // ② 查询全部在用设备
        List<AedDevice> devices = aedDeviceMapper.selectList(
                new LambdaQueryWrapper<AedDevice>().eq(AedDevice::getStatus, 0));
        if (devices.isEmpty()) {
            return 0;
        }

        // ③ 查询目标日期已存在的全部任务, 已有任务的设备跳过, 避免重复生成
        Set<Long> existedDeviceIds = this.list(new LambdaQueryWrapper<InspectionTask>()
                        .eq(InspectionTask::getPlanDate, planDate))
                .stream()
                .map(InspectionTask::getDeviceId)
                .collect(Collectors.toSet());

        // ④ 查询巡检人员角色及该角色下的全部用户, 无巡检人员时直接报错
        SysRole inspectorRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "inspector"));
        if (inspectorRole == null) {
            throw new BusinessException("系统暂无巡检人员, 无法生成任务");
        }
        List<SysUser> inspectors = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRoleId, inspectorRole.getId()));
        if (inspectors.isEmpty()) {
            throw new BusinessException("系统暂无巡检人员, 无法生成任务");
        }

        // 统计每位巡检人员当前待巡检任务数(负载均衡依据), 生成过程中同步累加
        Map<Long, Long> pendingCountMap = this.list(new LambdaQueryWrapper<InspectionTask>()
                        .eq(InspectionTask::getStatus, 0))
                .stream()
                .filter(task -> task.getInspectorId() != null)
                .collect(Collectors.groupingBy(InspectionTask::getInspectorId, Collectors.counting()));

        // ⑤ 逐台设备生成待巡检任务, 分配给待巡检任务最少的巡检人员
        List<InspectionTask> newTasks = new ArrayList<>();
        for (AedDevice device : devices) {
            if (existedDeviceIds.contains(device.getId())) {
                continue;
            }
            Long inspectorId = inspectors.stream()
                    .min(Comparator.comparingLong(user -> pendingCountMap.getOrDefault(user.getId(), 0L)))
                    .map(SysUser::getId)
                    .orElseThrow(() -> new BusinessException("系统暂无巡检人员, 无法生成任务"));
            pendingCountMap.merge(inspectorId, 1L, Long::sum);

            InspectionTask task = new InspectionTask();
            task.setDeviceId(device.getId());
            task.setInspectorId(inspectorId);
            task.setPlanDate(planDate);
            task.setStatus(0);
            newTasks.add(task);
        }
        if (!newTasks.isEmpty()) {
            this.saveBatch(newTasks);
        }
        return newTasks.size();
    }

    @Override
    public PageResult<InspectionTaskVO> pageQuery(InspectionTaskQueryDTO dto) {
        LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<>();
        // 巡检人员角色只能查看分配给自己的任务
        if ("inspector".equals(UserContext.getRoleCode())) {
            wrapper.eq(InspectionTask::getInspectorId, UserContext.getUserId());
        }
        wrapper.eq(dto.getStatus() != null, InspectionTask::getStatus, dto.getStatus())
                .eq(dto.getDeviceId() != null, InspectionTask::getDeviceId, dto.getDeviceId())
                .eq(dto.getPlanDate() != null, InspectionTask::getPlanDate, dto.getPlanDate())
                .orderByDesc(InspectionTask::getId);
        Page<InspectionTask> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        return toPageResult(page.getRecords(), page.getTotal());
    }

    @Override
    public InspectionTaskVO getDetail(Long id) {
        InspectionTask task = this.getById(id);
        if (task == null) {
            throw new BusinessException("巡检任务不存在");
        }
        // 巡检人员角色只能查看分配给自己的任务
        if ("inspector".equals(UserContext.getRoleCode())
                && !Objects.equals(task.getInspectorId(), UserContext.getUserId())) {
            throw new BusinessException(403, "无权查看该任务");
        }
        return toVO(task, loadDeviceMap(Collections.singletonList(task)),
                loadUserMap(Collections.singletonList(task)));
    }

    /**
     * 组装分页结果并补充设备编号/名称与巡检员姓名(设备或用户已删除时对应字段为 null)
     */
    private PageResult<InspectionTaskVO> toPageResult(List<InspectionTask> tasks, Long total) {
        Map<Long, AedDevice> deviceMap = loadDeviceMap(tasks);
        Map<Long, SysUser> userMap = loadUserMap(tasks);
        List<InspectionTaskVO> records = tasks.stream()
                .map(task -> toVO(task, deviceMap, userMap))
                .collect(Collectors.toList());
        PageResult<InspectionTaskVO> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        return result;
    }

    /**
     * 批量查询任务关联设备(逻辑删除的设备自动过滤, 查询不到则返回 null)
     */
    private Map<Long, AedDevice> loadDeviceMap(List<InspectionTask> tasks) {
        Set<Long> deviceIds = tasks.stream()
                .map(InspectionTask::getDeviceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (deviceIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return aedDeviceMapper.selectList(new LambdaQueryWrapper<AedDevice>().in(AedDevice::getId, deviceIds))
                .stream()
                .collect(Collectors.toMap(AedDevice::getId, Function.identity()));
    }

    /**
     * 批量查询任务关联巡检人员(逻辑删除的用户自动过滤, 查询不到则返回 null)
     */
    private Map<Long, SysUser> loadUserMap(List<InspectionTask> tasks) {
        Set<Long> userIds = tasks.stream()
                .map(InspectionTask::getInspectorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds))
                .stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
    }

    /**
     * 实体转 VO
     */
    private InspectionTaskVO toVO(InspectionTask task, Map<Long, AedDevice> deviceMap, Map<Long, SysUser> userMap) {
        InspectionTaskVO vo = new InspectionTaskVO();
        vo.setId(task.getId());
        vo.setDeviceId(task.getDeviceId());
        vo.setInspectorId(task.getInspectorId());
        vo.setPlanDate(task.getPlanDate());
        vo.setStatus(task.getStatus());
        vo.setResult(task.getResult());
        vo.setCompleteTime(task.getCompleteTime());
        vo.setCreateTime(task.getCreateTime());
        AedDevice device = task.getDeviceId() == null ? null : deviceMap.get(task.getDeviceId());
        if (device != null) {
            vo.setDeviceCode(device.getDeviceCode());
            vo.setDeviceName(device.getDeviceName());
        }
        SysUser user = task.getInspectorId() == null ? null : userMap.get(task.getInspectorId());
        if (user != null) {
            vo.setInspectorName(user.getRealName());
        }
        return vo;
    }
}
