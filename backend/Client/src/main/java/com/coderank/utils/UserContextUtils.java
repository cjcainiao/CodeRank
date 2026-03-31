package com.coderank.utils;


/**
 * 用户上下文工具类
 */
public final class UserContextUtils {

    /**
     * 用户上下文
     */
    private static final ThreadLocal<Long> USER_CONTEXT = new ThreadLocal<>();


    /**
     * 设置用户id
     *
     * @param userId
     */
    public static void setUser(Long userId) {
        USER_CONTEXT.set(userId);
    }

    /**
     * 获取用户id
     *
     * @return
     */
    public static Long getUser() {
        return USER_CONTEXT.get();
    }

    /**
     * 清空用户id
     */
    public static void clear() {
        USER_CONTEXT.remove();
    }

}
