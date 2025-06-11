package com.niceone.sharekit.config;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.niceone.sharekit.service.FirebaseService;
import com.niceone.sharekit.domain.user.User;
import com.niceone.sharekit.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/// Firebase 토큰 검사 필터
@RequiredArgsConstructor
@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private final FirebaseService firebaseService;
    private final UserService userService;
    private static final Logger logger = Logger.getLogger(FirebaseTokenFilter.class.getName());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String idToken = header.substring(7);

        try {
            // firebase id 토큰 검증
            FirebaseToken decodedToken = firebaseService.verifyToken(idToken);
            String uid = decodedToken.getUid();

            User user = userService.findByUid(uid)
                    .orElseThrow(() -> new NoSuchElementException("User not found with UID: " + uid));

            Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toList());

            // 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (FirebaseAuthException e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid firebase token: " + e.getMessage());
            logger.severe("Firebase token validation failed: " + e.getMessage());
            return;
        } catch (NoSuchElementException e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found in database: " + e.getMessage());
            logger.severe("User not found: " + e.getMessage());
            return;
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error: " + e.getMessage());
            logger.severe("Authentication error: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }
}