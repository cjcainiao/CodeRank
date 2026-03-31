package com.coderank.entity.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户基本信息
 */
@Data
public class UserInfoVO implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别(0 未知 ,1 男 ,2 女)
     */
    private Integer sex;

    /**
     * 用户状态(0-禁用，1-正常，2-锁定)
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 登录ip
     */
    private String loginIp;


}