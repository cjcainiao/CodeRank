package com.coderank.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录请求接收
 */
@Data
public class LoginDTO implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    /**
     * 登录分类
     */
    @NotBlank(message = "登录分类不能为空")
    private String type;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

}
