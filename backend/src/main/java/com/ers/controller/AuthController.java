package com.ers.controller;

import com.ers.dto.ChangePasswordRequest;
import com.ers.dto.LoginRequest;
import com.ers.dto.RegisterRequest;
import com.ers.dto.Result;
import com.ers.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> result = authService.login(request);
        return Result.success("登录成功", result);
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> result = authService.register(request);
        return Result.success("注册成功", result);
    }

    @PutMapping("/change-password")
    public Result<Void> changePassword(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = authService.getUserFromToken(token).getId();
        authService.changePassword(userId, request);
        return Result.success("密码修改成功", null);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return Result.success("退出登录成功", null);
    }
}
