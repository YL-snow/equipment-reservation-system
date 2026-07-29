package com.ers.service;

import com.ers.entity.User;
import com.ers.entity.UserRole;
import com.ers.exception.BusinessException;
import com.ers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public Page<User> getStudents(String name, String userId, Pageable pageable) {
        boolean hasName = name != null && !name.isEmpty();
        boolean hasUserId = userId != null && !userId.isEmpty();
        
        if (hasName && hasUserId) {
            return userRepository.findByNameContainingAndUserIdContaining(name, userId, pageable);
        }
        if (hasName) {
            return userRepository.findByNameContaining(name, pageable);
        }
        if (hasUserId) {
            return userRepository.findByUserIdContaining(userId, pageable);
        }
        return userRepository.findAll(pageable);
    }

    public User getStudentById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException("ACCESS_DENIED", "无权访问");
        }
        return user;
    }

    @Transactional
    public User updateStudent(Long id, String name) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        if (user.getRole() != UserRole.STUDENT) {
            throw new BusinessException("ACCESS_DENIED", "无权修改");
        }
        user.setName(name);
        return userRepository.save(user);
    }

    @Transactional
    public void resetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        user.setPassword(passwordEncoder.encode("Ab123456"));
        userRepository.save(user);
    }

    @Transactional
    public User updateRole(Long id, UserRole role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public User toggleBlacklist(Long id, boolean blacklisted) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        user.setIsBlacklisted(blacklisted);
        if (blacklisted) {
            user.setBlacklistedUntil(LocalDateTime.now().plusDays(30));
        } else {
            user.setBlacklistedUntil(null);
        }
        return userRepository.save(user);
    }

    public boolean isBlacklisted(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        if (user.getIsBlacklisted() != null && user.getIsBlacklisted()) {
            if (user.getBlacklistedUntil() != null && user.getBlacklistedUntil().isAfter(LocalDateTime.now())) {
                return true;
            } else {
                user.setIsBlacklisted(false);
                user.setBlacklistedUntil(null);
                userRepository.save(user);
                return false;
            }
        }
        return false;
    }

    public LocalDateTime getBlacklistedUntil(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        return user.getBlacklistedUntil();
    }

    @Transactional
    public void incrementOverdueCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        user.setOverdueCount(user.getOverdueCount() + 1);
        
        if (user.getOverdueCount() >= 2) {
            user.setIsBlacklisted(true);
            user.setBlacklistedUntil(LocalDateTime.now().plusDays(30));
        }
        
        userRepository.save(user);
    }
}
