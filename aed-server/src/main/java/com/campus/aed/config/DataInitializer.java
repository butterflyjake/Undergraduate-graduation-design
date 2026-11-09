package com.campus.aed.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.aed.entity.SysRole;
import com.campus.aed.entity.SysUser;
import com.campus.aed.mapper.SysRoleMapper;
import com.campus.aed.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化: 系统首次启动时创建默认管理员账号 admin/123456
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(null) > 0) {
            return;
        }
        SysRole adminRole = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "admin"));
        if (adminRole == null) {
            log.warn("未找到 admin 角色, 请先执行数据库初始化脚本 sql/init.sql");
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRealName("系统管理员");
        admin.setRoleId(adminRole.getId());
        admin.setStatus(1);
        userMapper.insert(admin);
        log.info("已创建默认管理员账号: admin / 123456");
    }
}
