package com.ers.service;

import com.ers.dto.ChangePasswordRequest;
import com.ers.dto.LoginRequest;
import com.ers.dto.RegisterRequest;
import com.ers.entity.User;
import com.ers.entity.UserRole;
import com.ers.exception.BusinessException;
import com.ers.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final String JWT_SECRET = "ers-secret-key-2026-equipment-reservation-system";
    private static final long JWT_EXPIRATION = 2 * 60 * 60 * 1000L;

    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();

    private Pattern letterPattern = Pattern.compile("[a-zA-Z]");
    private Pattern digitPattern = Pattern.compile("[0-9]");

    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "学号/工号或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("INVALID_CREDENTIALS", "学号/工号或密码错误");
        }

        if (user.getRole() == UserRole.SYSTEM) {
            throw new BusinessException("INVALID_CREDENTIALS", "系统用户无法登录");
        }

        String token = generateToken(user);

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("userId", user.getUserId());
        result.put("name", user.getName());
        result.put("role", user.getRole().name());
        result.put("token", token);
        result.put("isBlacklisted", user.getIsBlacklisted());
        result.put("blacklistedUntil", user.getBlacklistedUntil());

        return result;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new BusinessException("USER_ID_EXISTS", "该学号已存在");
        }

        if (!isValidPassword(request.getPassword())) {
            throw new BusinessException("INVALID_PASSWORD", "密码强度不足，需包含字母和数字，长度不少于8位");
        }

        User user = new User();
        user.setUserId(request.getUserId());
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.STUDENT);
        user.setIsBlacklisted(false);
        user.setOverdueCount(0);

        User savedUser = userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("id", savedUser.getId());
        result.put("userId", savedUser.getUserId());
        result.put("name", savedUser.getName());
        result.put("role", savedUser.getRole().name());

        return result;
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("INVALID_PASSWORD", "原密码错误");
        }

        if (!isValidPassword(request.getNewPassword())) {
            throw new BusinessException("INVALID_PASSWORD", "新密码强度不足，需包含字母和数字，长度不少于8位");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        tokenBlacklist.add(token);
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }

    public Claims parseToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new BusinessException("UNAUTHORIZED", "未授权");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (isTokenBlacklisted(token)) {
            throw new BusinessException("UNAUTHORIZED", "登录已失效");
        }

        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public User getUserFromToken(String token) {
        Claims claims = parseToken(token);
        Long userId = claims.get("userId", Long.class);
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    }

    private String generateToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("userRole", user.getRole().name());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUserId())
                .issuedAt(new Date())
                .expiration(Date.from(LocalDateTime.now().plusHours(2).atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(key)
                .compact();
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = letterPattern.matcher(password).find();
        boolean hasDigit = digitPattern.matcher(password).find();
        return hasLetter && hasDigit;
    }
}
