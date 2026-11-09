package com.campus.aed.security;

import com.campus.aed.entity.SysRole;
import com.campus.aed.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 登录用户信息(Spring Security UserDetails 实现)
 */
@Data
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private SysUser user;

    private SysRole role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 角色编码转换为 ROLE_ 前缀的权限标识, 供 @PreAuthorize("hasRole('admin')") 使用
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public Long getId() {
        return user.getId();
    }

    public String getRoleCode() {
        return role.getRoleCode();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() != null && user.getStatus() == 1;
    }
}
