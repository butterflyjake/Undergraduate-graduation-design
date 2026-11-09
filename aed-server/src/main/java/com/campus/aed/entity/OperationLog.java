package com.campus.aed.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体(由切面自动写入, 不做修改/删除)
 */
@Data
@TableName("operation_log")
public class OperationLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户名 */
    private String username;

    /** 操作描述(控制器.方法名) */
    private String operation;

    /** 请求方式与地址 */
    private String method;

    /** 请求参数(JSON, 最多1000字符) */
    private String params;

    /** 请求IP */
    private String ip;

    /** 操作时间(自动填充) */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
