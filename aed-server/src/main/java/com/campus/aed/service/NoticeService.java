package com.campus.aed.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.NoticeDTO;
import com.campus.aed.dto.NoticeQueryDTO;
import com.campus.aed.entity.Notice;
import com.campus.aed.vo.NoticeVO;

/**
 * 公告管理 Service
 */
public interface NoticeService extends IService<Notice> {

    /**
     * 公告分页查询(非管理员强制只查已发布公告)
     */
    PageResult<NoticeVO> pageQuery(NoticeQueryDTO dto);

    /**
     * 公告详情(非管理员不可查看草稿)
     */
    NoticeVO detail(Long id);

    /**
     * 发布/保存公告(status=1 时记录发布时间)
     */
    void create(NoticeDTO dto);

    /**
     * 修改公告
     */
    void update(NoticeDTO dto);

    /**
     * 删除公告(逻辑删除)
     */
    void delete(Long id);
}
