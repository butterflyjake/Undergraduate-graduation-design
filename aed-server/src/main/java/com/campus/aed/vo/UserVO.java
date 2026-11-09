package com.campus.aed.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户分页查询响应
 */
@Data
public class UserVO {

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

    /** 角色名称 */
    private String roleName;

    /** 状态: 1-启用 0-禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
