package com.coderank.enums;


import com.coderank.exception.BusinessException;

/**
 * 登录类型枚举(便于开发人员知道)
 */
public enum LoginType {

    username("usernameLogin", 1);


    /**
     * 获取登录类型
     * @param code
     * @return
     */
    public static String getByType(Integer code) {
        for (LoginType loginType : values()) {
            if (loginType.code.equals(code)) return loginType.type;
        }
        throw new BusinessException("当前不支持这种登录类型");
    }

    /**
     * 获取登录编码
     *
     * @param type
     * @return
     */
    public static Integer getByCode(String type) {
        for (LoginType loginType : values()) {
            if (loginType.type.equals(type)) return loginType.code;
        }
        throw new BusinessException("当前不支持这种登录类型");
    }

    /**
     * 登录类型
     */
    private String type;

    private Integer code;


    LoginType(String type, Integer code) {
        this.type = type;
        this.code = code;
    }
}
