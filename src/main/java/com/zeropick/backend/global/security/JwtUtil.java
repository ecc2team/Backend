package com.zeropick.backend.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    // 1. 비밀키 준비: yml의 일반 문자열(secret) -> 자바 전용 SecretKey 객체로 변환
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // 2. 토큰 생성 (generateToken)
    public String generateToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)         // 식별자(이메일) 넣기
                .issuedAt(now)          // 발급 시간
                .expiration(expiry)     // 만료 시간
                .signWith(key)          // 서버 비밀키로 Signature
                .compact();             // 최종 "aaaaa.bbbbb.ccccc" 문자열 반환
    }

    // 3. 토큰 해독 및 검증 (parseClaims)
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)            // 서버 비밀키 세팅
                .build()
                .parseSignedClaims(token)   // Signature 검증
                .getPayload();              // 검증 통과 시 Claims 반환
    }

    // 4. 이메일 추출
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // 5. 유효성 검사
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


}
