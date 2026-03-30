package com.coderank.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 脱离 Spring Security 的密码加密工具类
 */
public class PasswordEncryptUtils {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(10);

    /**
     * 加密密码
     * @param rawPassword
     * @return
     */
    public static String encrypt(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    /**
     * 校验密码
     * @param rawPassword
     * @param encodedPassword
     * @return
     */
    public static boolean verify(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }
}