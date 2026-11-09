package com.campus.aed.controller;

import com.campus.aed.common.PageResult;
import com.campus.aed.common.Result;
import com.campus.aed.dto.UserDTO;
import com.campus.aed.entity.SysRole;
import com.campus.aed.service.UserService;
import com.campus.aed.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理控制器: 用户增删改查与角色列表(仅系统管理员可操作)
 * 类级映射 /api, 方法级补全 /user/... 与 /role/list 完整路径
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "用户管理")
public class UserController {

    private final UserService userService;

    /** 用户分页查询: keyword 模糊匹配账号或姓名, 支持 roleId/status 过滤 */
    @GetMapping("/user/page")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "用户分页查询")
    public Result<PageResult<UserVO>> page(UserDTO dto) {
        return Result.ok(userService.pageQuery(dto));
    }

    /** 新增用户 */
    @PostMapping("/user")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "新增用户")
    public Result<Void> create(@Validated(UserDTO.Create.class) @RequestBody UserDTO dto) {
        userService.createUser(dto);
        return Result.ok();
    }

    /** 修改用户(密码可空, 非空则重置) */
    @PutMapping("/user")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "修改用户")
    public Result<Void> update(@Validated(UserDTO.Update.class) @RequestBody UserDTO dto) {
        userService.updateUser(dto);
        return Result.ok();
    }

    /** 删除用户(逻辑删除) */
    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "删除用户")
    public Result<Void> delete(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return Result.ok();
    }

    /** 角色列表(下拉选择用) */
    @GetMapping("/role/list")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "角色列表")
    public Result<List<SysRole>> roleList() {
        return Result.ok(userService.listRoles());
    }
}
