package com.campus.aed.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.HandleWarningDTO;
import com.campus.aed.dto.WarningQueryDTO;
import com.campus.aed.entity.ExpiryWarning;
import com.campus.aed.vo.WarningVO;

/**
 * 到期预警服务接口
 */
public interface ExpiryWarningService extends IService<ExpiryWarning> {

    /**
     * 预警分页查询(按预警日期倒序, 支持状态/类型过滤)
     */
    PageResult<WarningVO> pageQuery(WarningQueryDTO dto);

    /**
     * 处理预警: 仅未处理状态可处理, 填写处理说明后置为已处理
     */
    WarningVO handle(Long id, HandleWarningDTO dto);

    /**
     * 扫描配件有效期生成到期预警(定时任务每天 07:00 调用):
     * ① 扫描"使用中"配件: 已过期 → 类型2; 30天内到期 → 类型1;
     * ② 同一配件已有未处理预警(任意类型)则不重复生成;
     * ③ 已被更换/已删除的配件的未处理预警自动置为已处理
     */
    void scanExpiry();
}
