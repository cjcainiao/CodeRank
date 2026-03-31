package com.coderank.enums;

import com.coderank.exception.BusinessException;

/**
 * 注册类型枚举(便于开发人员知道)
 */
public enum RegisterType {
    username("usernameRegister",1);


    /**
     * 获取注册类型
     * @param code
     * @return
     */
    public static String getByType(Integer code) {
        for (RegisterType registerType : values()) {
            if (registerType.code.equals(code)) return registerType.type;
        }
        throw new BusinessException("当前不支持这种注册类型");
    }

    /**
     * 获取注册编码
     *
     * @param type
     * @return
     */
    public static Integer getByCode(String type) {
        for (RegisterType registerType : values()) {
            if (registerType.type.equals(type)) return registerType.code;
        }
        throw new BusinessException("当前不支持这种注册类型");
    }

    RegisterType(String type, Integer code) {
        this.type = type;
        this.code = code;
    }

    /**
     * 注册类型
     */
    private String type;

    private Integer code;

}
