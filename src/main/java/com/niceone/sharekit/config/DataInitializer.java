package com.niceone.sharekit.config;

import com.niceone.sharekit.domain.user.Role;
import com.niceone.sharekit.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Initializing default roles...");

        // 생성할 기본 역할 목록
        List<String> roleNames = Arrays.asList("ROLE_PRE_AUTH_USER", "ROLE_USER", "ROLE_ADMIN");

        for (String roleName : roleNames) {
            if (!roleRepository.findByName(roleName).isPresent()) {
                // 역할이 존재하지 않으면 새로 생성하여 저장
                roleRepository.save(new Role(roleName));
                log.info("Created role: {}", roleName);
            } else {
                log.info("Role {} already exists.", roleName);
            }
        }
        log.info("Default roles initialization complete.");
    }
}