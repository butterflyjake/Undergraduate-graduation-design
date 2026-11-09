package com.campus.aed.dto;

import com.campus.aed.common.PageQuery;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户请求参数: 继承 PageQuery 承担分页查询参数, 同时承担新增/修改提交参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends PageQuery {

    /** 新增校验分组 */
    public interface Create {
    }

    /** 修改校验分组 */
    public interface Update {
    }

    /** 用户ID(修改时必填) */
    @NotNull(message = "用户ID不能为空", groups = Update.class)
    private Long id;

    /** 登录账号 */
    @NotBlank(message = "登录账号不能为空", groups = {Create.class, Update.class})
    @Size(max = 50, message = "登录账号长度不能超过50个字符", groups = {Create.class, Update.class})
    private String username;

    /** 密码(新增必填, 修改可空; 非空时 BCrypt 加密后重置) */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "密码不能为空", groups = Create.class)
    @Size(min = 6, max = 20, message = "密码长度需在6-20位之间", groups = {Create.class, Update.class})
    private String password;

    /** 姓名 */
    @Size(max = 50, message = "姓名长度不能超过50个字符", groups = {Create.class, Update.class})
    private String realName;

    /** 联系电话 */
    @Size(max = 20, message = "联系电话长度不能超过20个字符", groups = {Create.class, Update.class})
    private String phone;

    /** 角色ID */
    @NotNull(message = "角色不能为空", groups = {Create.class, Update.class})
    private Long roleId;

    /** 状态: 1-启用 0-禁用 */
    private Integer status;

    /** 分页查询关键字: 模糊匹配登录账号或姓名 */
    private String keyword;
}
