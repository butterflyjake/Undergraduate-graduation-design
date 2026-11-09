package com.campus.aed.vo;

import lombok.Data;

/**
 * 登录响应 / 当前登录用户信息
 */
@Data
public class LoginVO {

    /** 令牌(获取用户信息接口中为空) */
    private String token;

    /** 用户ID */
    private Long id;

    /** 登录账号 */
    private String username;

    /** 姓名 */
    private String realName;

    /** 联系电话 */
    private String phone;

    /** 角色ID */
    private Long roleId;

    /** 角色编码: admin / device_manager / inspector */
    private String roleCode;

    /** 角色名称 */
    private String roleName;
}
