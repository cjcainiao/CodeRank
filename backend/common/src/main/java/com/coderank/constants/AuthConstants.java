package com.coderank.constants;

/**
 * 认证相关常量。
 */
public final class AuthConstants {

    private AuthConstants() {
    }

    /** 用户信息的 Redis Key 前缀。 */
    public static final String LOGIN_USER_INFO_CACHE_PREFIX = "auth:login:info:";

    /** 用户登录凭证的 Redis Key 前缀。 */
    public static final String LOGIN_USER_TOKEN_CACHE_PREFIX = "auth:login:token:";

    /** 图形验证码的 Redis Key 前缀。 */
    public static final String CAPTCHA_CACHE_PREFIX = "auth:captcha:";

    /** 图形验证码有效期（秒）。 */
    public static final long CAPTCHA_EXPIRE_SECONDS = 60;

    /** 图形验证码字符数。 */
    public static final int CAPTCHA_LENGTH = 4;

    /** 用户名登录策略的 Spring Bean 名称。 */
    public static final String USERNAME_LOGIN_STRATEGY = "usernameLogin";

    /** 用户名注册策略的 Spring Bean 名称。 */
    public static final String USERNAME_REGISTER_STRATEGY = "usernameRegister";

    /** 用户名最大字符数。 */
    public static final int MAX_USERNAME_LENGTH = 50;

    /** 登录密码最大字符数。 */
    public static final int MAX_LOGIN_PASSWORD_LENGTH = 128;

    /** 注册密码最小字符数。 */
    public static final int MIN_REGISTER_PASSWORD_LENGTH = 6;

    /** BCrypt 支持的密码最大字节数。 */
    public static final int MAX_REGISTER_PASSWORD_BYTES = 72;
}
