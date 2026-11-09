package com.campus.aed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.LogQueryDTO;
import com.campus.aed.entity.OperationLog;
import com.campus.aed.mapper.OperationLogMapper;
import com.campus.aed.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 操作日志服务实现: 日志由切面写入, 仅提供分页查询
 */
@Service
@RequiredArgsConstructor
public class LogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements LogService {

    @Override
    public PageResult<OperationLog> pageQuery(LogQueryDTO dto) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        // 关键字模糊匹配操作用户名或操作描述
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(OperationLog::getUsername, dto.getKeyword())
                    .or().like(OperationLog::getOperation, dto.getKeyword()));
        }
        // 按操作时间倒序
        wrapper.orderByDesc(OperationLog::getCreateTime);
        Page<OperationLog> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        PageResult<OperationLog> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords());
        return result;
    }
}
