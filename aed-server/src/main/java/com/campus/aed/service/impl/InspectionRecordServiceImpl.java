package com.campus.aed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.aed.common.BusinessException;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.InspectionItemDTO;
import com.campus.aed.dto.InspectionRecordQueryDTO;
import com.campus.aed.dto.SubmitInspectionDTO;
import com.campus.aed.entity.AedDevice;
import com.campus.aed.entity.InspectionItem;
import com.campus.aed.entity.InspectionRecord;
import com.campus.aed.entity.InspectionTask;
import com.campus.aed.entity.RepairOrder;
import com.campus.aed.entity.SysUser;
import com.campus.aed.mapper.AedDeviceMapper;
import com.campus.aed.mapper.InspectionItemMapper;
import com.campus.aed.mapper.InspectionRecordMapper;
import com.campus.aed.mapper.InspectionTaskMapper;
import com.campus.aed.mapper.RepairOrderMapper;
import com.campus.aed.mapper.SysUserMapper;
import com.campus.aed.service.InspectionRecordService;
import com.campus.aed.util.UserContext;
import com.campus.aed.vo.InspectionRecordDetailVO;
import com.campus.aed.vo.InspectionRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 巡检记录服务实现
 */
@Service
@RequiredArgsConstructor
public class InspectionRecordServiceImpl extends ServiceImpl<InspectionRecordMapper, InspectionRecord> implements InspectionRecordService {

    private final InspectionTaskMapper inspectionTaskMapper;

    private final InspectionItemMapper inspectionItemMapper;

    private final RepairOrderMapper repairOrderMapper;

    private final AedDeviceMapper aedDeviceMapper;

    private final SysUserMapper sysUserMapper;

    /** 固定 6 项巡检检查项清单(与API文档巡检项模板一致) */
    private static final List<String> FIXED_ITEM_NAMES = List.of(
            "主机外观完好无破损", "电源指示灯显示正常", "电极片在有效期内",
            "电池电量充足", "机柜箱体完好", "设备周边无障碍物");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(SubmitInspectionDTO dto) {
        Long userId = UserContext.getUserId();

        // 校验任务: 存在性、归属巡检员、允许提交的状态
        InspectionTask task = inspectionTaskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException("巡检任务不存在");
        }
        if (!Objects.equals(task.getInspectorId(), userId)) {
            throw new BusinessException("无权提交该任务");
        }
        // 待巡检(0)与已逾期(2)任务均可提交, 已逾期允许被分配人补交, 保证巡检闭环
        if (!Objects.equals(task.getStatus(), 0) && !Objects.equals(task.getStatus(), 2)) {
            throw new BusinessException("该任务已提交过或状态异常");
        }

        // 校验检查项: 必须恰好为固定 6 项且名称一致, 防止伪造/省略检查项规避维修工单
        Set<String> itemNames = dto.getItems().stream()
                .map(InspectionItemDTO::getItemName)
                .collect(Collectors.toSet());
        if (itemNames.size() != FIXED_ITEM_NAMES.size() || !FIXED_ITEM_NAMES.containsAll(itemNames)) {
            throw new BusinessException("检查明细必须为固定6项检查项");
        }

        // 汇总巡检结果: 明细中任一检查项异常则整体为异常
        boolean abnormal = dto.getItems().stream().anyMatch(item -> Objects.equals(item.getItemResult(), 2));
        int result = abnormal ? 2 : 1;
        LocalDateTime now = LocalDateTime.now();

        // 原子占用任务(待巡检/已逾期→已完成): 条件更新按影响行数判断, 防止并发重复提交命中唯一键
        InspectionTask taskUpdate = new InspectionTask();
        taskUpdate.setStatus(1);
        taskUpdate.setResult(result);
        taskUpdate.setCompleteTime(now);
        if (inspectionTaskMapper.update(taskUpdate, new LambdaQueryWrapper<InspectionTask>()
                .eq(InspectionTask::getId, task.getId())
                .in(InspectionTask::getStatus, 0, 2)) == 0) {
            throw new BusinessException("该任务已提交过或状态异常");
        }

        // 创建巡检记录
        InspectionRecord record = new InspectionRecord();
        record.setTaskId(task.getId());
        record.setDeviceId(task.getDeviceId());
        record.setInspectorId(userId);
        record.setInspectTime(now);
        record.setResult(result);
        record.setSummary(dto.getSummary());
        this.save(record);

        // 逐项保存检查明细
        for (InspectionItemDTO itemDTO : dto.getItems()) {
            InspectionItem item = new InspectionItem();
            item.setRecordId(record.getId());
            item.setItemName(itemDTO.getItemName());
            item.setItemResult(itemDTO.getItemResult());
            item.setRemark(itemDTO.getRemark());
            inspectionItemMapper.insert(item);
        }

        // 存在异常项时自动创建维修工单(报修来源=巡检异常)
        if (result == 2) {
            String abnormalNames = dto.getItems().stream()
                    .filter(item -> Objects.equals(item.getItemResult(), 2))
                    .map(InspectionItemDTO::getItemName)
                    .collect(Collectors.joining("、"));
            String faultDesc = (StringUtils.hasText(dto.getSummary()) ? dto.getSummary() + "。" : "")
                    + "异常项: " + abnormalNames;

            RepairOrder order = new RepairOrder();
            order.setOrderNo(buildOrderNo());
            order.setDeviceId(task.getDeviceId());
            order.setReportType(1);
            order.setReporterId(userId);
            order.setFaultDesc(faultDesc);
            order.setStatus(0);
            repairOrderMapper.insert(order);
        }
    }

    @Override
    public PageResult<InspectionRecordVO> pageQuery(InspectionRecordQueryDTO dto) {
        LambdaQueryWrapper<InspectionRecord> wrapper = new LambdaQueryWrapper<>();
        // 巡检人员角色只能查看自己的记录
        if ("inspector".equals(UserContext.getRoleCode())) {
            wrapper.eq(InspectionRecord::getInspectorId, UserContext.getUserId());
        }
        wrapper.eq(dto.getDeviceId() != null, InspectionRecord::getDeviceId, dto.getDeviceId())
                .eq(dto.getResult() != null, InspectionRecord::getResult, dto.getResult())
                .orderByDesc(InspectionRecord::getInspectTime)
                .orderByDesc(InspectionRecord::getId);
        Page<InspectionRecord> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        return toPageResult(page.getRecords(), page.getTotal());
    }

    @Override
    public InspectionRecordDetailVO getDetail(Long id) {
        InspectionRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException("巡检记录不存在");
        }
        // 巡检人员角色只能查看自己的记录
        if ("inspector".equals(UserContext.getRoleCode())
                && !Objects.equals(record.getInspectorId(), UserContext.getUserId())) {
            throw new BusinessException(403, "无权查看该记录");
        }
        InspectionRecordDetailVO vo = new InspectionRecordDetailVO();
        fillBaseVO(vo, record, loadDeviceMap(Collections.singletonList(record)),
                loadUserMap(Collections.singletonList(record)));
        // 检查明细按明细ID升序
        List<InspectionRecordDetailVO.InspectionItemVO> items = inspectionItemMapper
                .selectList(new LambdaQueryWrapper<InspectionItem>()
                        .eq(InspectionItem::getRecordId, id)
                        .orderByAsc(InspectionItem::getId))
                .stream()
                .map(this::toItemVO)
                .collect(Collectors.toList());
        vo.setItems(items);
        return vo;
    }

    /**
     * 组装分页结果并补充设备编号/名称与巡检员姓名(设备或用户已删除时对应字段为 null)
     */
    private PageResult<InspectionRecordVO> toPageResult(List<InspectionRecord> records, Long total) {
        Map<Long, AedDevice> deviceMap = loadDeviceMap(records);
        Map<Long, SysUser> userMap = loadUserMap(records);
        List<InspectionRecordVO> voList = records.stream()
                .map(record -> {
                    InspectionRecordVO vo = new InspectionRecordVO();
                    fillBaseVO(vo, record, deviceMap, userMap);
                    return vo;
                })
                .collect(Collectors.toList());
        PageResult<InspectionRecordVO> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(voList);
        return result;
    }

    /**
     * 填充记录 VO 基础字段
     */
    private void fillBaseVO(InspectionRecordVO vo, InspectionRecord record,
                            Map<Long, AedDevice> deviceMap, Map<Long, SysUser> userMap) {
        vo.setId(record.getId());
        vo.setTaskId(record.getTaskId());
        vo.setDeviceId(record.getDeviceId());
        vo.setInspectorId(record.getInspectorId());
        vo.setInspectTime(record.getInspectTime());
        vo.setResult(record.getResult());
        vo.setSummary(record.getSummary());
        AedDevice device = record.getDeviceId() == null ? null : deviceMap.get(record.getDeviceId());
        if (device != null) {
            vo.setDeviceCode(device.getDeviceCode());
            vo.setDeviceName(device.getDeviceName());
        }
        SysUser user = record.getInspectorId() == null ? null : userMap.get(record.getInspectorId());
        if (user != null) {
            vo.setInspectorName(user.getRealName());
        }
    }

    /**
     * 检查明细实体转 VO
     */
    private InspectionRecordDetailVO.InspectionItemVO toItemVO(InspectionItem item) {
        InspectionRecordDetailVO.InspectionItemVO vo = new InspectionRecordDetailVO.InspectionItemVO();
        vo.setItemName(item.getItemName());
        vo.setItemResult(item.getItemResult());
        vo.setRemark(item.getRemark());
        return vo;
    }

    /**
     * 批量查询记录关联设备(逻辑删除的设备自动过滤, 查询不到则返回 null)
     */
    private Map<Long, AedDevice> loadDeviceMap(List<InspectionRecord> records) {
        Set<Long> deviceIds = records.stream()
                .map(InspectionRecord::getDeviceId)
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
     * 批量查询记录关联巡检人员(逻辑删除的用户自动过滤, 查询不到则返回 null)
     */
    private Map<Long, SysUser> loadUserMap(List<InspectionRecord> records) {
        Set<Long> userIds = records.stream()
                .map(InspectionRecord::getInspectorId)
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
     * 生成维修工单编号: WX + yyyyMMddHHmmss + 3位随机数字
     */
    private String buildOrderNo() {
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return "WX" + time + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
