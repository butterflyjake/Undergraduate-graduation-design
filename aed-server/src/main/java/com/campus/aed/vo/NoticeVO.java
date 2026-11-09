package com.campus.aed.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告 VO(在实体基础上补充 publisherName 发布人姓名)
 */
@Data
public class NoticeVO {

    /** 公告ID */
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 公告类型: 1-系统公告 2-急救科普 */
    private Integer noticeType;

    /** 发布人ID */
    private Long publisherId;

    /** 发布人姓名(冗余填充, 用户不存在时为 null) */
    private String publisherName;

    /** 状态: 1-已发布 0-草稿 */
    private Integer status;

    /** 发布时间(草稿为 null) */
    private LocalDateTime publishTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
