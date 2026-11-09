package com.campus.aed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.aed.common.BusinessException;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.LoginDTO;
import com.campus.aed.dto.PasswordDTO;
import com.campus.aed.dto.UserDTO;
import com.campus.aed.entity.SysRole;
import com.campus.aed.entity.SysUser;
import com.campus.aed.mapper.SysRoleMapper;
import com.campus.aed.mapper.SysUserMapper;
import com.campus.aed.security.JwtUtil;
import com.campus.aed.security.LoginUser;
import com.campus.aed.security.UserDetailsServiceImpl;
import com.campus.aed.service.UserService;
import com.campus.aed.util.UserContext;
import com.campus.aed.vo.LoginVO;
import com.campus.aed.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现: 登录认证与用户管理
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    /** 角色编码: 系统管理员 */
    private static final String ROLE_ADMIN = "admin";

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final UserDetailsServiceImpl userDetailsService;

    private final SysRoleMapper roleMapper;

    @Override
    public LoginVO login(LoginDTO dto) {
        // 加载用户, 用户不存在统一按"用户名或密码错误"处理, 避免暴露账号是否存在
        LoginUser loginUser;
        try {
            loginUser = (LoginUser) userDetailsService.loadUserByUsername(dto.getUsername());
        } catch (UsernameNotFoundException e) {
            throw new BusinessException("用户名或密码错误");
        }
        SysUser user = loginUser.getUser();
        // 禁用账号禁止登录, 但错误信息与密码错误统一返回, 避免暴露账号状态(防用户名枚举)
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("用户名或密码错误");
        }
        // BCrypt 密码校验
        if (!passwordEncoder.matches(dto.getPassword(), loginUser.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        SysRole role = loginUser.getRole();
        if (role == null) {
            throw new BusinessException("账号角色配置异常, 请联系管理员");
        }
        LoginVO vo = toLoginVO(user, role, null);
        // 校验通过, 签发 JWT
        vo.setToken(jwtUtil.generateToken(user.getId(), user.getUsername()));
        return vo;
    }

    @Override
    public LoginVO currentInfo() {
        // 当前用户信息由 JWT 过滤器每次请求从数据库加载, 直接使用
        LoginUser loginUser = UserContext.getLoginUser();
        SysRole role = loginUser.getRole();
        if (role == null) {
            throw new BusinessException("账号角色配置异常, 请联系管理员");
        }
        return toLoginVO(loginUser.getUser(), role, null);
    }

    @Override
    public void updatePassword(PasswordDTO dto) {
        Long userId = UserContext.getUserId();
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在或已被删除");
        }
        // 校验旧密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        // 新旧密码不能相同
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }
        // BCrypt 加密后重置(仅更新密码字段, 更新时间自动填充)
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(update);
    }

    @Override
    public PageResult<UserVO> pageQuery(UserDTO dto) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        // 关键字模糊匹配登录账号或姓名
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, dto.getKeyword())
                    .or().like(SysUser::getRealName, dto.getKeyword()));
        }
        // 按角色、状态过滤
        wrapper.eq(dto.getRoleId() != null, SysUser::getRoleId, dto.getRoleId());
        wrapper.eq(dto.getStatus() != null, SysUser::getStatus, dto.getStatus());
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> page = this.page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        // 一次性查出全部角色放 Map, 填充 roleName
        Map<Long, String> roleNameMap = roleMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleName));
        List<UserVO> records = page.getRecords().stream()
                .map(user -> toUserVO(user, roleNameMap.get(user.getRoleId())))
                .collect(Collectors.toList());
        PageResult<UserVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public void createUser(UserDTO dto) {
        // 登录账号唯一校验
        if (this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())) > 0) {
            throw new BusinessException("登录账号已存在");
        }
        // 状态白名单校验: 仅允许 1-启用 0-禁用
        if (dto.getStatus() != null && dto.getStatus() != 1 && dto.getStatus() != 0) {
            throw new BusinessException("状态取值不合法, 仅支持1-启用或0-禁用");
        }
        // 所选角色必须存在
        SysRole role = roleMapper.selectById(dto.getRoleId());
        if (role == null) {
            throw new BusinessException("所选角色不存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setRoleId(dto.getRoleId());
        // 状态未传时默认启用
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        try {
            this.save(user);
        } catch (DuplicateKeyException e) {
            // 唯一索引兜底: 并发写入或历史数据占用登录账号时, 转业务提示而非 500
            throw new BusinessException("登录账号已存在");
        }
    }

    @Override
    public void updateUser(UserDTO dto) {
        SysUser target = this.getById(dto.getId());
        if (target == null) {
            throw new BusinessException("用户不存在");
        }
        // 状态白名单校验: 仅允许 1-启用 0-禁用
        if (dto.getStatus() != null && dto.getStatus() != 1 && dto.getStatus() != 0) {
            throw new BusinessException("状态取值不合法, 仅支持1-启用或0-禁用");
        }
        Long currentUserId = UserContext.getUserId();
        // 用户保护: 不能禁用自己(非 1 一律视为禁用, 与登录过滤的判定口径一致)
        if (target.getId().equals(currentUserId) && dto.getStatus() != null && dto.getStatus() != 1) {
            throw new BusinessException("不能禁用自己");
        }
        // 账号变更时校验唯一
        if (!dto.getUsername().equals(target.getUsername())
                && this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())) > 0) {
            throw new BusinessException("登录账号已存在");
        }
        // 所选角色必须存在
        SysRole role = roleMapper.selectById(dto.getRoleId());
        if (role == null) {
            throw new BusinessException("所选角色不存在");
        }
        // 用户保护: 目标用户当前为启用状态的管理员, 且本次将禁用或变更其角色时,
        // 校验系统仍至少保留一个启用状态的系统管理员
        if (isEnabledAdmin(target)) {
            // 非 1 一律视为禁用, 防止传入 0/1 以外的值绕过保护
            boolean disabling = dto.getStatus() != null && dto.getStatus() != 1;
            boolean roleChanged = !dto.getRoleId().equals(target.getRoleId());
            if (disabling || roleChanged) {
                checkRemainingEnabledAdmin(target.getId());
            }
        }
        SysUser update = new SysUser();
        update.setId(dto.getId());
        update.setUsername(dto.getUsername());
        update.setRealName(dto.getRealName());
        update.setPhone(dto.getPhone());
        update.setRoleId(dto.getRoleId());
        update.setStatus(dto.getStatus());
        // 密码非空时 BCrypt 加密后重置
        if (StringUtils.hasText(dto.getPassword())) {
            update.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        try {
            this.updateById(update);
        } catch (DuplicateKeyException e) {
            // 唯一索引兜底: 并发修改或历史数据占用登录账号时, 转业务提示而非 500
            throw new BusinessException("登录账号已存在");
        }
    }

    @Override
    public void deleteUser(Long id) {
        SysUser target = this.getById(id);
        if (target == null) {
            throw new BusinessException("用户不存在");
        }
        // 用户保护: 不能删除自己
        if (target.getId().equals(UserContext.getUserId())) {
            throw new BusinessException("不能删除自己");
        }
        // 用户保护: 删除启用状态的管理员前, 校验系统仍至少保留一个启用状态的系统管理员
        if (isEnabledAdmin(target)) {
            checkRemainingEnabledAdmin(target.getId());
        }
        // 逻辑删除前改写登录账号(追加 _del_ 与ID后缀), 释放 uk_username 唯一索引占用,
        // 使被删除账号的同名账号可重建; 数据行仍保留, 符合逻辑删除可恢复的设计
        String suffix = "_del_" + target.getId();
        String origin = target.getUsername();
        String renamed = origin.substring(0, Math.min(origin.length(), 50 - suffix.length())) + suffix;
        SysUser rename = new SysUser();
        rename.setId(target.getId());
        rename.setUsername(renamed);
        this.updateById(rename);
        this.removeById(id);
    }

    @Override
    public List<SysRole> listRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
    }

    /** 判断用户是否为启用状态的系统管理员 */
    private boolean isEnabledAdmin(SysUser user) {
        if (user.getStatus() == null || user.getStatus() != 1) {
            return false;
        }
        SysRole role = roleMapper.selectById(user.getRoleId());
        return role != null && ROLE_ADMIN.equals(role.getRoleCode());
    }

    /** 校验排除指定用户后, 系统仍至少存在一个启用状态的系统管理员 */
    private void checkRemainingEnabledAdmin(Long excludeUserId) {
        SysRole adminRole = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, ROLE_ADMIN));
        if (adminRole == null) {
            throw new BusinessException("系统角色数据异常, 缺少系统管理员角色");
        }
        long count = this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRoleId, adminRole.getId())
                .eq(SysUser::getStatus, 1)
                .ne(SysUser::getId, excludeUserId));
        if (count == 0) {
            throw new BusinessException("系统必须至少保留一个启用状态的系统管理员");
        }
    }

    /** 组装登录响应/当前用户信息 */
    private LoginVO toLoginVO(SysUser user, SysRole role, String token) {
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setRoleId(user.getRoleId());
        vo.setRoleCode(role == null ? null : role.getRoleCode());
        vo.setRoleName(role == null ? null : role.getRoleName());
        return vo;
    }

    /** 组装用户分页响应 */
    private UserVO toUserVO(SysUser user, String roleName) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setRoleId(user.getRoleId());
        vo.setRoleName(roleName);
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
