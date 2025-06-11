package com.niceone.sharekit.config;

import com.niceone.sharekit.domain.user.User;
import com.niceone.sharekit.service.JwtTokenProvider;
import com.niceone.sharekit.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = resolveToken(request);

        if (StringUtils.hasText(jwt)) {
            try {
                if (jwtTokenProvider.validateToken(jwt)) {
                    String uid = jwtTokenProvider.getUidFromToken(jwt);

                    User user = userService.findByUid(uid)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found with UID: " + uid + " from JWT."));

                    if (user.getStudentId() == null || user.getStudentId().isEmpty() ||
                            user.getPassword() == null || user.getPassword().isEmpty()) {
                        log.warn("JWT authentication attempt for user UID: {} who has not completed profile setup (studentId or password missing).", uid);
                    }

                    Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role.getName()))
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT authentication successful for user UID: {}, authorities: {}", uid, authorities);
                } else {
                    log.warn("Invalid JWT token provided.");
                }
            } catch (UsernameNotFoundException e) {
                log.warn("JWT authentication failed: User not found. {}", e.getMessage());
                SecurityContextHolder.clearContext();
            } catch (ExpiredJwtException e) {
                log.warn("JWT authentication failed: Token expired. {}", e.getMessage());
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Token expired\", \"message\": \"" + e.getMessage() + "\"}");
                response.setContentType("application/json");
                return;
            } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
                log.warn("JWT authentication failed: Invalid token. {}", e.getMessage());
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid token\", \"message\": \"" + e.getMessage() + "\"}");
                response.setContentType("application/json");
                return;
            } catch (Exception e) {
                log.error("JWT authentication failed with unexpected error.", e);
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Authentication error\", \"message\": \"" + e.getMessage() + "\"}");
                response.setContentType("application/json");
                return;
            }
        } else {
            log.trace("No JWT token found in request header.");
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}