package com.coderank.entity.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 注册返回
 */
@Data
public class RegisterVO implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;


    /**
     * 是否注册成功
     */
    private Boolean success = true;

}
