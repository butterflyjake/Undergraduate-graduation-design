package com.campus.aed.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.aed.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 公告实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notice")
public class Notice extends BaseEntity {

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 类型: 1-系统公告 2-急救科普 */
    private Integer noticeType;

    /** 发布人ID */
    private Long publisherId;

    /** 状态: 1-已发布 0-草稿 */
    private Integer status;

    /** 发布时间 */
    private LocalDateTime publishTime;
}
