package com.coderank.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 注册请求
 */
@Data
public class RegisterDTO implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    /**
     * 注册类型
     */
    private Integer type;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 确认密码
     */
    private String confirmPassword;
}
