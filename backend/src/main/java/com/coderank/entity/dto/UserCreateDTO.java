package com.coderank.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增用户请求参数。
 */
@Data
public class UserCreateDTO {

    /** 登录账号 */
    @NotBlank(message = "登录账号不能为空")
    @Size(max = 50, message = "登录账号长度不能超过 50")
    private String username;

    /** 登录密码 */
    @NotBlank(message = "登录密码不能为空")
    @Size(min = 6, max = 32, message = "登录密码长度须为 6 到 32 位")
    private String password;

    /** 昵称 */
    @Size(max = 50, message = "昵称长度不能超过 50")
    private String nickname;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100")
    private String email;

    /** 手机号 */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 头像地址 */
    @Size(max = 255, message = "头像地址长度不能超过 255")
    private String avatar;

    /** 个性签名 */
    @Size(max = 255, message = "个性签名长度不能超过 255")
    private String signature;

    /** 性别：0 未知，1 男，2 女 */
    private Integer gender;

    /** 角色：0 普通用户，1 管理员 */
    private Integer role;
}
