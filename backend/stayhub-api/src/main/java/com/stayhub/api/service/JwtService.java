package com.stayhub.api.service;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JwtService {

    // Inject chuỗi secret từ file application.properties vào đây
    @Value("${jwt.secret}")
    private String secretString;

    // Hàm helper để khởi tạo SecretKey động dựa trên chuỗi cấu hình cấu hình
    private SecretKey getSigningKey() {
        return new SecretKeySpec(
                secretString.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // Dùng key động
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String generateToken(String username, Map<String, Object> extraClaims) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24H
                .signWith(getSigningKey()) // Dùng key động
                .compact();
    }
}