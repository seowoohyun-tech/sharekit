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
                .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // SecurityConfig.java (authorizeHttpRequests 부분)
                .authorizeHttpRequests(authz -> authz
                        // 1. 인증 없이 접근 허용
                        .requestMatchers(
                                PathRequest.toStaticResources().atCommonLocations(),

                                // 정적 리소스 (css, js, images)
                                AntPathRequestMatcher.antMatcher("/js/**"),
                                AntPathRequestMatcher.antMatcher("/css/**"),
                                AntPathRequestMatcher.antMatcher("/images/**"),
                                // HTML 페이지 경로 (GET 요청)
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/login"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/signup"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/profile"),
                                AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/equipment-list"),
                                // API 인증/인가 관련 경로
                                AntPathRequestMatcher.antMatcher("/api/auth/signup"),
                                AntPathRequestMatcher.antMatcher("/api/auth/login"),
                                // 기타 공개 경로
                                AntPathRequestMatcher.antMatcher("/public/**"),
                                AntPathRequestMatcher.antMatcher("/error"),
                                AntPathRequestMatcher.antMatcher("/favicon.ico"),
                                AntPathRequestMatcher.antMatcher("/v3/api-docs/**"),
                                AntPathRequestMatcher.antMatcher("/swagger-ui/**"),
                                AntPathRequestMatcher.antMatcher("/swagger-resources/**")
                        ).permitAll()

                        // 2. Firebase 토큰 인증 필요한 경로
                        .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.PUT, "/api/users/me/profile"))
                        .hasAnyAuthority("ROLE_PRE_AUTH_USER", "ROLE_USER", "ROLE_ADMIN")

                        // 3. JWT 인증 필요한 API 경로들
                        .requestMatchers(AntPathRequestMatcher.antMatcher("/api/**"))
                        .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        // 4. 지정되지 않은 나머지 요청은 인증 요구
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}