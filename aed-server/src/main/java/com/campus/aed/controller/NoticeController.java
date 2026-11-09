package com.campus.aed.controller;

import com.campus.aed.common.PageResult;
import com.campus.aed.common.Result;
import com.campus.aed.dto.NoticeDTO;
import com.campus.aed.dto.NoticeQueryDTO;
import com.campus.aed.service.NoticeService;
import com.campus.aed.vo.NoticeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告管理控制器
 */
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
@Tag(name = "公告管理")
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 公告分页查询(所有登录用户可访问, 非管理员仅可见已发布公告)
     */
    @GetMapping("/page")
    @Operation(summary = "公告分页查询")
    public Result<PageResult<NoticeVO>> page(@Validated NoticeQueryDTO dto) {
        return Result.ok(noticeService.pageQuery(dto));
    }

    /**
     * 公告详情(所有登录用户可访问, 非管理员不可查看草稿)
     */
    @GetMapping("/{id}")
    @Operation(summary = "公告详情")
    public Result<NoticeVO> detail(@PathVariable Long id) {
        return Result.ok(noticeService.detail(id));
    }

    /**
     * 发布/保存公告(仅管理员)
     */
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "发布公告")
    public Result<Void> create(@Valid @RequestBody NoticeDTO dto) {
        noticeService.create(dto);
        return Result.ok();
    }

    /**
     * 修改公告(仅管理员)
     */
    @PutMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "修改公告")
    public Result<Void> update(@Valid @RequestBody NoticeDTO dto) {
        noticeService.update(dto);
        return Result.ok();
    }

    /**
     * 删除公告(仅管理员, 逻辑删除)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "删除公告")
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.ok();
    }
}
