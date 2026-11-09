package com.campus.aed.dto;

import com.campus.aed.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公告分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "公告分页查询参数")
public class NoticeQueryDTO extends PageQuery {

    /** 关键字(公告标题模糊匹配) */
    @Schema(description = "关键字, 模糊匹配公告标题")
    private String keyword;

    /** 状态: 1-已发布 0-草稿(仅管理员可过滤, 非管理员强制查已发布) */
    @Schema(description = "状态: 1-已发布 0-草稿, 仅管理员可过滤")
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 1, message = "状态值不合法")
    private Integer status;

    /** 公告类型: 1-系统公告 2-急救科普(仅管理员可过滤) */
    @Schema(description = "公告类型: 1-系统公告 2-急救科普, 仅管理员可过滤")
    @Min(value = 1, message = "公告类型不合法")
    @Max(value = 2, message = "公告类型不合法")
    private Integer noticeType;
}
