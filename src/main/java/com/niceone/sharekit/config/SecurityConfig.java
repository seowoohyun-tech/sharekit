package com.niceone.sharekit.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final FirebaseTokenFilter firebaseTokenFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(firebaseTokenFilter, JwtAuthenticationFilter.class)

                .authorizeHttpRequests(authz -> authz
                        // 1. 인증 없이 접근 허용
                        .requestMatchers(
                                PathRequest.toStaticResources().atCommonLocations(),
                                AntPathRequestMatcher.antMatcher("/js/**"),
                                AntPathRequestMatcher.antMatcher("/css/**"),
                                AntPathRequestMatcher.antMatcher("/images/**"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/login"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/signup"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/profile"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/equipment-list"),
                                AntPathRequestMatcher.antMatcher("/api/auth/signup"),
                                AntPathRequestMatcher.antMatcher("/api/auth/login"),
                                AntPathRequestMatcher.antMatcher("/public/**"),
                                AntPathRequestMatcher.antMatcher("/error"),
                                AntPathRequestMatcher.antMatcher("/favicon.ico"),
                                AntPathRequestMatcher.antMatcher("/v3/api-docs/**"),
                                AntPathRequestMatcher.antMatcher("/swagger-ui/**"),
                                AntPathRequestMatcher.antMatcher("/swagger-resources/**")
                        ).permitAll()

                        // 2. 프로필 업데이트: PRE_AUTH_USER 또는 USER 역할이 필요
                        // PRE_AUTH_USER는 최초 등록을 위해, USER는 정보 수정을 위해 접근
                        .requestMatchers(HttpMethod.PUT, "/api/users/me/profile")
                        .hasAnyRole("PRE_AUTH_USER", "USER")

                        // 3. 관리자 전용 API (향후 확장 예정)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 4. 나머지 모든 /api/** 경로는 USER 또는 ADMIN 역할 필요
                        // 물품 대여/반납, 내역 조회 등 대부분의 기능
                        .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN")

                        // 5. 그 외 모든 요청은 일단 인증만 되면 허용
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}