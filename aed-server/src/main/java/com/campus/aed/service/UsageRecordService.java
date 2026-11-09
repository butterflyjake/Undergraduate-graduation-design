package com.campus.aed.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.UsageDTO;
import com.campus.aed.dto.UsageQueryDTO;
import com.campus.aed.entity.UsageRecord;
import com.campus.aed.vo.UsageVO;

/**
 * AED使用记录 Service
 */
public interface UsageRecordService extends IService<UsageRecord> {

    /** 使用记录分页查询(按使用发生时间倒序) */
    PageResult<UsageVO> pageQuery(UsageQueryDTO dto);

    /** 登记使用记录(登记人固定为当前登录用户) */
    UsageVO add(UsageDTO dto);

    /** 使用记录详情 */
    UsageVO getDetail(Long id);
}
