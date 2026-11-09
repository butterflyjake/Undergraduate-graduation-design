package com.campus.aed.common;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类: 主键、创建时间、更新时间、逻辑删除标记
 * createTime/updateTime 由 MyBatis-Plus 自动填充, deleted 逻辑删除自动生效
 */
@Data
public class BaseEntity implements Serializable {

    /** 主键ID, 自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建时间(自动填充) */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间(自动填充) */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记: 0-未删除 1-已删除 */
    @TableLogic
    @JsonIgnore
    private Integer deleted;
}
