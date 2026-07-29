package com.ers.controller;

import com.ers.dto.PageResult;
import com.ers.dto.Result;
import com.ers.entity.User;
import com.ers.service.AuthService;
import com.ers.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    public Result<PageResult<User>> getStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String userId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> result = userService.getStudents(name, userId, pageable);
        return Result.success(PageResult.from(result));
    }

    @GetMapping("/{id}")
    public Result<User> getStudentById(@PathVariable Long id) {
        User user = userService.getStudentById(id);
        return Result.success(user);
    }

    @PutMapping("/{id}/blacklist")
    public Result<User> toggleBlacklist(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        boolean blacklisted = Boolean.TRUE.equals(body.get("isBlacklisted"));
        User user = userService.toggleBlacklist(id, blacklisted);
        String msg = blacklisted ? "已加入失信名单" : "已移出失信名单";
        return Result.success(msg, user);
    }

    @PutMapping("/{id}")
    public Result<User> updateStudent(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String name = body.get("name");
        User user = userService.updateStudent(id, name);
        return Result.success("修改成功", user);
    }

    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success("密码已重置为 Ab123456", null);
    }

    @PutMapping("/{id}/role")
    public Result<User> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String role = body.get("role");
        User user = userService.updateRole(id, com.ers.entity.UserRole.valueOf(role));
        return Result.success("角色更新成功", user);
    }
}
