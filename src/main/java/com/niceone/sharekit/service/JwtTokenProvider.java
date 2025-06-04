package com.niceone.sharekit.service;

import com.niceone.sharekit.domain.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.token-validity-in-seconds}")
    private long tokenValidityInSeconds;

    private Key secretKey;
    private long tokenValidityInMilliseconds;

    private static final String AUTHORITIES_KEY = "auth";

    @PostConstruct
    protected void init() {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }

    /**
     * 사용자 정보를 기반으로 JWT 토큰을 생성합니다.
     * @param authentication Spring Security의 Authentication 객체 (내부에 UserDetails 또는 User 객체 포함)
     * @return 생성된 JWT 문자열
     */
    public String generateToken(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal(); // User 객체를 principal로 사용한다고 가정
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date validity = new Date(now.getTime() + this.tokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(userPrincipal.getUid()) // 토큰 제목 (사용자 UID)
                .claim(AUTHORITIES_KEY, authorities) // 권한 정보 저장
                .claim("email", userPrincipal.getEmail()) // 추가 정보 (선택)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * User 객체로부터 직접 JWT 토큰을 생성합니다. (AuthController에서 사용하기 위함)
     * @param user User 객체
     * @return 생성된 JWT 문자열
     */
    public String generateToken(User user) {
        String authorities = "";
        if (user.getRole() != null && !user.getRole().isEmpty()) {
            authorities = user.getRole();
        }

        Date now = new Date();
        Date validity = new Date(now.getTime() + this.tokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(user.getUid())
                .claim(AUTHORITIES_KEY, authorities)
                .claim("email", user.getEmail())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


    /**
     * JWT 토큰에서 사용자 UID (Subject)를 추출합니다.
     * @param token JWT 토큰 문자열
     * @return 사용자 UID
     */
    public String getUidFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * JWT 토큰에서 Authentication 객체를 생성합니다.
     * @param token JWT 토큰 문자열
     * @return Spring Security의 Authentication 객체
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        String uid = claims.getSubject();
        String email = claims.get("email", String.class); // 이메일 클레임 가져오기

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .filter(auth -> !auth.trim().isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // User 객체를 principal로 사용. 여기서는 UID와 email만 있는 간단한 User 객체 생성.
        // 실제로는 DB에서 User를 조회해서 사용하거나, UserDetails를 구현한 객체를 사용.
        // 여기서는 JWT 필터에서 DB 조회를 하므로, 여기서는 User의 핵심 정보만 담은 객체를 생성해도 됨.
        // 또는 UserDetails 객체를 직접 반환하도록 수정할 수도 있음.
        User principal = User.builder().uid(uid).email(email).build(); // 역할 정보는 authorities에 있음

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * JWT 토큰의 유효성을 검증합니다.
     * @param token JWT 토큰 문자열
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}