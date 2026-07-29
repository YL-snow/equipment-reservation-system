package com.ers.config;

import com.ers.entity.User;
import com.ers.entity.UserRole;
import com.ers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        userRepository.findByUserId("admin").ifPresentOrElse(
            user -> {
                if (!passwordEncoder.matches("Ab123456", user.getPassword())) {
                    user.setPassword(passwordEncoder.encode("Ab123456"));
                    userRepository.save(user);
                    log.info("Admin password reset successfully");
                }
            },
            () -> {
                User admin = new User();
                admin.setUserId("admin");
                admin.setName("管理员");
                admin.setPassword(passwordEncoder.encode("Ab123456"));
                admin.setRole(UserRole.ADMIN);
                admin.setIsBlacklisted(false);
                admin.setOverdueCount(0);
                userRepository.save(admin);
                log.info("Admin user created");
            }
        );
    }
}
