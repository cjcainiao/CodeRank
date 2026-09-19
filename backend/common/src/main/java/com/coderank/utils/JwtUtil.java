package com.coderank.utils;

import com.coderank.constants.JwtConstants;
import com.coderank.context.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class JwtUtil {

    private final SecretKey secretKey;

    /** 过期时间（毫秒），默认 2 小时 */
    private final long expiration;

    public JwtUtil(
            @Value("${" + JwtConstants.SECRET_KEY + ":template-jwt-secret-key-please-change-in-production-32bytes}") String secret,
            @Value("${" + JwtConstants.EXPIRATION_KEY + ":7200000}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * 根据登录用户生成 token。
     */
    public String createToken(LoginUser user) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .claim(JwtConstants.CLAIM_USER, user)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 token 为登录用户；token 无效/过期时抛出异常，由调用方处理。
     */
    public LoginUser parseToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get(JwtConstants.CLAIM_USER, LoginUser.class);
    }

    /** 获取 Token 有效期，单位为毫秒。 */
    public long getExpiration() {
        return expiration;
    }
}
