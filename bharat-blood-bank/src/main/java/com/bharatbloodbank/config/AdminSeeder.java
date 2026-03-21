package com.bharatbloodbank.config;

import com.bharatbloodbank.entity.User;
import com.bharatbloodbank.enums.Role;
import com.bharatbloodbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .name(adminName)
                .role(Role.ADMIN)
                .enabled(true)
                .build();
            userRepository.save(admin);
            log.info("✅ Default admin created: {}", adminEmail);
        } else {
            log.info("ℹ️  Admin already exists, skipping seed.");
        }
    }
}
