package com.weihanyinian.website.config;

import com.weihanyinian.website.entity.User;
import com.weihanyinian.website.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin-password:}")
    private String adminPassword;

    @Value("${app.bootstrap.admin-sync-on-startup:false}")
    private boolean syncOnStartup;

    public AdminUserBootstrapRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!syncOnStartup || adminPassword == null || adminPassword.isEmpty()) {
            return;
        }

        String username = "admin";

        if (userRepository.findByUsername(username).isPresent()) {
            // Update existing admin password
            User admin = userRepository.findByUsername(username).get();
            admin.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(admin);
        } else {
            // Create new admin
            User admin = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(adminPassword))
                    .role("ADMIN")
                    .build();
            userRepository.save(admin);
        }
    }
}
