package com.campus.aed.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.DeviceDTO;
import com.campus.aed.dto.DeviceQueryDTO;
import com.campus.aed.entity.AedDevice;
import com.campus.aed.vo.DeviceVO;

import java.util.List;

/**
 * 设备台账服务接口
 */
public interface AedDeviceService extends IService<AedDevice> {

    /** 设备分页查询(keyword 模糊匹配编号/名称, 支持楼宇/状态过滤) */
    PageResult<DeviceVO> pageQuery(DeviceQueryDTO dto);

    /** 设备简表(下拉选择用, 不分页, 按设备编号升序) */
    List<AedDevice> listAll();

    /** 设备详情 */
    DeviceVO getDetail(Long id);

    /** 新增设备(校验设备编号唯一) */
    DeviceVO create(DeviceDTO dto);

    /** 修改设备(校验设备编号唯一, 排除自身) */
    DeviceVO update(DeviceDTO dto);

    /** 快速变更设备状态 */
    void updateStatus(Long id, Integer status);

    /** 删除设备(存在维修中工单或待巡检任务时拒绝) */
    void delete(Long id);
}
