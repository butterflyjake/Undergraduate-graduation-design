package com.campus.aed.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 公告新增/修改请求参数
 */
@Data
@Schema(description = "公告新增/修改请求参数")
public class NoticeDTO {

    /** 公告ID(修改时必填, 新增时忽略) */
    @Schema(description = "公告ID, 修改时必填")
    private Long id;

    /** 公告标题 */
    @Schema(description = "公告标题")
    @NotBlank(message = "公告标题不能为空")
    private String title;

    /** 公告内容 */
    @Schema(description = "公告内容")
    @NotBlank(message = "公告内容不能为空")
    private String content;

    /** 公告类型: 1-系统公告 2-急救科普 */
    @Schema(description = "公告类型: 1-系统公告 2-急救科普")
    @NotNull(message = "公告类型不能为空")
    @Min(value = 1, message = "公告类型不合法")
    @Max(value = 2, message = "公告类型不合法")
    private Integer noticeType;

    /** 状态: 1-已发布 0-草稿 */
    @Schema(description = "状态: 1-已发布 0-草稿")
    @NotNull(message = "公告状态不能为空")
    @Min(value = 0, message = "公告状态不合法")
    @Max(value = 1, message = "公告状态不合法")
    private Integer status;
}
