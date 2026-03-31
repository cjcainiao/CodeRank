package com.coderank.utils;

import com.coderank.enums.ResultCode;
import com.coderank.exception.BusinessException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * jwt工具类
 */
@Component
@Data
public class JwtUtils {

    /**
     * 密钥
     */
    @Value("${jwt.secretKey:default-secret-key-32bit-1234567890}")
    private String secretKey;

    /**
     * 过期时间(确认token)
     */
    @Value("${jwt.expire:3600}")
    private Long expire;

    /**
     * 刷新token过期时间
     */
    @Value("${jwt.refresh:86400}")
    private Long refresh;

    /**
     * 确认token
     *
     * @param claims
     * @return
     */
    public String flush_token(Map<String, String> claims) {
        return generateToken(claims, refresh);
    }

    /**
     * 生成token
     *
     * @param claims
     * @return
     */
    public String createToken(Map<String, String> claims) {
        return generateToken(claims, expire);
    }

    /**
     * 生成token
     *
     * @param claims
     * @param expireSeconds
     * @return
     */
    private String generateToken(Map<String, String> claims, Long expireSeconds) {
        SecretKey secret = Keys.hmacShaKeyFor(secretKey.getBytes());
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .setSubject("Login")
                .setClaims(claims)            // 设置自定义载荷
                .setIssuedAt(now)             // 设置签发时间
                .setExpiration(expireDate)    // 设置过期时间
                .signWith(secret, SignatureAlgorithm.HS256)
                .compact();                   // 生成最终Token
    }


    /**
     * 解析token
     *
     * @param token
     * @return
     * @throws JwtException
     */
    public Claims parseToken(String token) throws JwtException {
        try {
            SecretKey secret = Keys.hmacShaKeyFor(secretKey.getBytes());
            return Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) { //token过期
            throw new BusinessException(ResultCode.TOKEN_IS_EXPIRED.getCode(), ResultCode.TOKEN_IS_EXPIRED.getMsg());
        } catch (SecurityException e) { // token被篡改
            throw new BusinessException(ResultCode.NO_PERMISSION.getCode(), ResultCode.NO_PERMISSION.getMsg());
        } catch (Exception e) {
            throw new BusinessException("token非法");
        }
    }


}
