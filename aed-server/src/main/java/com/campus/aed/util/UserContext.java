package com.campus.aed.util;

import com.campus.aed.common.BusinessException;
import com.campus.aed.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户上下文工具类
 */
public class UserContext {

    /** 获取当前登录用户, 未登录抛出业务异常 */
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new BusinessException(401, "未登录");
    }

    /** 当前用户ID */
    public static Long getUserId() {
        return getLoginUser().getId();
    }

    /** 当前用户名 */
    public static String getUsername() {
        return getLoginUser().getUsername();
    }

    /** 当前用户角色编码 */
    public static String getRoleCode() {
        return getLoginUser().getRoleCode();
    }
}
