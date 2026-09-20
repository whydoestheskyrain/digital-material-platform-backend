package com.trade.virtual_trade.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

public class JwtUtil {

    // 原始密钥字符串（建议至少 32 个字符）
    private static final String SECRET_STRING = "yourSecretKeyYourProject2026!@#$%^&*()";
    // 生成安全的 Key 对象（静态初始化，确保密钥固定）
    private static final Key SECRET_KEY;

    static {
        // 对原始字符串进行 Base64 编码（可选，增加熵）
        String base64Secret = Base64.getEncoder().encodeToString(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
        SECRET_KEY = Keys.hmacShaKeyFor(base64Secret.getBytes(StandardCharsets.UTF_8));
    }

    private static final long EXPIRE = 86400000L; // 24小时

    // 生成 token
    public static String generateToken(Integer userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)  // 使用 Key 对象签名
                .compact();
    }

    // 从 token 中解析 userId
    public static Integer getUserId(String token) {
        try {
            String subject = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)  // 使用 Key 对象解析
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            return Integer.parseInt(subject);
        } catch (Exception e) {
            return null;
        }
    }
}