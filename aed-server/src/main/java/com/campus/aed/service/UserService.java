package com.campus.aed.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.aed.common.PageResult;
import com.campus.aed.dto.LoginDTO;
import com.campus.aed.dto.PasswordDTO;
import com.campus.aed.dto.UserDTO;
import com.campus.aed.entity.SysRole;
import com.campus.aed.entity.SysUser;
import com.campus.aed.vo.LoginVO;
import com.campus.aed.vo.UserVO;

import java.util.List;

/**
 * 用户服务: 登录认证与用户管理
 */
public interface UserService extends IService<SysUser> {

    /** 登录: 校验账号密码并签发 JWT */
    LoginVO login(LoginDTO dto);

    /** 获取当前登录用户信息 */
    LoginVO currentInfo();

    /** 修改自己的密码 */
    void updatePassword(PasswordDTO dto);

    /** 用户分页查询 */
    PageResult<UserVO> pageQuery(UserDTO dto);

    /** 新增用户 */
    void createUser(UserDTO dto);

    /** 修改用户 */
    void updateUser(UserDTO dto);

    /** 删除用户(逻辑删除) */
    void deleteUser(Long id);

    /** 角色列表(下拉选择用) */
    List<SysRole> listRoles();
}
