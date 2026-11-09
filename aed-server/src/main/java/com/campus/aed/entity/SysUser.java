package com.campus.aed.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.campus.aed.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 登录账号 */
    private String username;

    /** 密码(BCrypt 加密) */
    private String password;

    /** 姓名 */
    private String realName;

    /** 联系电话 */
    private String phone;

    /** 角色ID */
    private Long roleId;

    /** 状态: 1-启用 0-禁用 */
    private Integer status;
}
