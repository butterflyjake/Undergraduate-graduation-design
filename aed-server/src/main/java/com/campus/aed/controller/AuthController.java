package com.campus.aed.controller;

import com.campus.aed.common.Result;
import com.campus.aed.dto.LoginDTO;
import com.campus.aed.dto.PasswordDTO;
import com.campus.aed.service.UserService;
import com.campus.aed.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器: 登录、当前用户信息、修改密码
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证登录")
public class AuthController {

    private final UserService userService;

    /** 登录(已放行, 无需登录态), 返回 token 与用户信息 */
    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(userService.login(dto));
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/info")
    @Operation(summary = "获取当前登录用户信息")
    public Result<LoginVO> info() {
        return Result.ok(userService.currentInfo());
    }

    /** 修改自己的密码 */
    @PutMapping("/password")
    @Operation(summary = "修改自己的密码")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordDTO dto) {
        userService.updatePassword(dto);
        return Result.ok();
    }
}
