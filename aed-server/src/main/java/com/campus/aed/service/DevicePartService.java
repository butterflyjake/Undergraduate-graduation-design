package com.campus.aed.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.PartDTO;
import com.campus.aed.dto.PartQueryDTO;
import com.campus.aed.entity.DevicePart;
import com.campus.aed.vo.PartVO;

import java.util.List;

/**
 * 设备配件服务接口
 */
public interface DevicePartService extends IService<DevicePart> {

    /** 配件分页查询(deviceId/partType/status 过滤, 按创建时间倒序) */
    PageResult<PartVO> pageQuery(PartQueryDTO dto);

    /** 某设备配件列表(deviceId/status 过滤, 不分页) */
    List<PartVO> listQuery(PartQueryDTO dto);

    /** 新增配件(自动替换同设备同类型旧配件, 联动关闭其未处理预警) */
    PartVO add(PartDTO dto);

    /** 修改配件 */
    PartVO update(PartDTO dto);

    /** 删除配件(逻辑删除) */
    void delete(Long id);
}
