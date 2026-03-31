package com.coderank.enums;


/**
 * 响应码枚举类
 */
public enum ResultCode {


    SECCESS(200, "操作成功"),
    USER_NOT_FOUND(40000,"未找到该用户"),
    PASSWORD_ERROR(40001,"密码错误"),
    ACCOUNT_LOCKED(40003,"账号已锁定,请联系管理员"),
    PARAMETER_ERROR(40004,"参数错误"),
    TWOPASSWOED_ERROR(40005,"两次密码不一致"),
    USERNAME_ISUSE(40006,"用户名已被使用"),
    USER_NOT_LOGIN(40007,"用户未登录"),
    NO_PERMISSION(40008, "没有权限"),
    TOKEN_IS_EXPIRED(40009, "token已过期"),
    LOGIN_IS_EXPIRED(40010, "登录已过期"),
    QUESTIONTITLE_ISUSE(40011,"题目已存在"),
    QUESTION_NOT_EXISTS(40012,"题目不存在"),
    TASK_CREATEFAILURE(40013,"任务创建失败");


    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应信息
     */
    private final String msg;


    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
