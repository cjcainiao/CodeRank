package com.coderank.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类：BCrypt 单向加盐哈希。
 * 注册时 encode 存库，登录时 matches 校验。
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    /** 明文密码加盐哈希 */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /** 校验明文与已存哈希是否匹配 */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
