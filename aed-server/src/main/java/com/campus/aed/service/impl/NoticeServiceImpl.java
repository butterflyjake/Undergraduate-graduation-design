package com.campus.aed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.aed.common.BusinessException;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.NoticeDTO;
import com.campus.aed.dto.NoticeQueryDTO;
import com.campus.aed.entity.Notice;
import com.campus.aed.entity.SysUser;
import com.campus.aed.mapper.NoticeMapper;
import com.campus.aed.mapper.SysUserMapper;
import com.campus.aed.service.NoticeService;
import com.campus.aed.util.UserContext;
import com.campus.aed.vo.NoticeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 公告管理 Service 实现
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    /** 已发布状态 */
    private static final int STATUS_PUBLISHED = 1;

    private final SysUserMapper sysUserMapper;

    @Override
    public PageResult<NoticeVO> pageQuery(NoticeQueryDTO dto) {
        boolean isAdmin = "admin".equals(UserContext.getRoleCode());
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        // 关键字模糊匹配公告标题
        wrapper.like(StringUtils.hasText(dto.getKeyword()), Notice::getTitle, dto.getKeyword());
        if (isAdmin) {
            // 管理员可按状态/类型过滤
            wrapper.eq(dto.getStatus() != null, Notice::getStatus, dto.getStatus())
                    .eq(dto.getNoticeType() != null, Notice::getNoticeType, dto.getNoticeType());
        } else {
            // 非管理员强制只查已发布公告(业务规则: 公告可见性)
            wrapper.eq(Notice::getStatus, STATUS_PUBLISHED);
        }
        // 排序: 已发布在前, 再按发布时间倒序(空值排后), 创建时间倒序兜底
        wrapper.last("ORDER BY status DESC, (publish_time IS NULL), publish_time DESC, create_time DESC");
        Page<Notice> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<NoticeVO> records = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        // 批量填充发布人姓名, 避免逐条查询
        fillPublisherNames(records);
        PageResult<NoticeVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public NoticeVO detail(Long id) {
        Notice notice = this.getById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        // 非管理员不可查看草稿(统一提示"公告不存在", 避免泄露草稿存在性)
        boolean isAdmin = "admin".equals(UserContext.getRoleCode());
        if (!isAdmin && !Objects.equals(notice.getStatus(), STATUS_PUBLISHED)) {
            throw new BusinessException("公告不存在");
        }
        NoticeVO vo = toVO(notice);
        fillPublisherNames(List.of(vo));
        return vo;
    }

    @Override
    public void create(NoticeDTO dto) {
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setNoticeType(dto.getNoticeType());
        notice.setStatus(dto.getStatus());
        // 发布人固定为当前登录用户
        notice.setPublisherId(UserContext.getUserId());
        // 已发布则记录发布时间, 草稿不设置发布时间
        if (Objects.equals(dto.getStatus(), STATUS_PUBLISHED)) {
            notice.setPublishTime(LocalDateTime.now());
        }
        this.save(notice);
    }

    @Override
    public void update(NoticeDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("公告ID不能为空");
        }
        Notice notice = this.getById(dto.getId());
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setNoticeType(dto.getNoticeType());
        notice.setStatus(dto.getStatus());
        // 发布人更新为当前登录用户
        notice.setPublisherId(UserContext.getUserId());
        // 已发布则(重新)记录发布时间; 草稿不携带发布时间, 改回草稿需清空旧值(与 create 及 NoticeVO 契约一致)
        if (Objects.equals(dto.getStatus(), STATUS_PUBLISHED)) {
            notice.setPublishTime(LocalDateTime.now());
        } else {
            notice.setPublishTime(null);
        }
        this.updateById(notice);
    }

    @Override
    public void delete(Long id) {
        // 逻辑删除自动生效, 先校验存在性给出统一提示
        if (this.getById(id) == null) {
            throw new BusinessException("公告不存在");
        }
        this.removeById(id);
    }

    /**
     * 实体转 VO(发布人姓名由 fillPublisherNames 统一填充)
     */
    private NoticeVO toVO(Notice notice) {
        NoticeVO vo = new NoticeVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setContent(notice.getContent());
        vo.setNoticeType(notice.getNoticeType());
        vo.setPublisherId(notice.getPublisherId());
        vo.setStatus(notice.getStatus());
        vo.setPublishTime(notice.getPublishTime());
        vo.setCreateTime(notice.getCreateTime());
        vo.setUpdateTime(notice.getUpdateTime());
        return vo;
    }

    /**
     * 批量填充发布人姓名, 关联用户不存在时保持 null
     */
    private void fillPublisherNames(List<NoticeVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> publisherIds = records.stream()
                .map(NoticeVO::getPublisherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SysUser> userMap = publisherIds.isEmpty() ? Map.of()
                : sysUserMapper.selectBatchIds(publisherIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, u -> u));
        for (NoticeVO vo : records) {
            SysUser user = userMap.get(vo.getPublisherId());
            if (user != null) {
                vo.setPublisherName(user.getRealName());
            }
        }
    }
}
