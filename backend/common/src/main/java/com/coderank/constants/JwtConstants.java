package com.coderank.constants;

/**
 * JWT 相关常量
 */
public final class JwtConstants {

    private JwtConstants() {
    }

    /** 存放登录用户对象的 claim 名 */
    public static final String CLAIM_USER = "user";

    /** JWT 密钥配置项 */
    public static final String SECRET_KEY = "jwt.secret";

    /** JWT 过期时间配置项 */
    public static final String EXPIRATION_KEY = "jwt.expiration";
}
