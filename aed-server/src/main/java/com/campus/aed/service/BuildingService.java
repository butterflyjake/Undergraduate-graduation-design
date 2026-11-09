package com.campus.aed.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.aed.dto.BuildingDTO;
import com.campus.aed.entity.Building;

import java.util.List;

/**
 * 楼宇管理 Service
 */
public interface BuildingService extends IService<Building> {

    /**
     * 查询全部楼宇(不分页, 按id升序)
     */
    List<Building> listAll();

    /**
     * 新增楼宇(名称唯一校验)
     */
    void create(BuildingDTO dto);

    /**
     * 修改楼宇
     */
    void update(BuildingDTO dto);

    /**
     * 删除楼宇(存在设备时禁止删除)
     */
    void delete(Long id);
}
