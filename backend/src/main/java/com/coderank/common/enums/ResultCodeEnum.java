package com.coderank.common.enums;

import lombok.Getter;

/**
 * 统一响应码。
 */
@Getter
public enum ResultCodeEnum {

    /** 成功 */
    SUCCESS(200, "操作成功"),

    /** 参数问题 */
    BAD_REQUEST(400, "请求参数有误"),

    /** 未认证 */
    UNAUTHORIZED(401, "未登录或登录已过期"),

    /** 无权限 */
    FORBIDDEN(403, "没有操作权限"),

    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    /** 唯一性冲突 */
    CONFLICT(409, "数据已存在"),

    /** 业务失败 */
    BUSINESS_ERROR(500, "业务处理失败"),

    /** 系统异常 */
    SYSTEM_ERROR(500, "系统繁忙，请稍后再试"),

    /** 远程服务异常 */
    REMOTE_ERROR(502, "远程服务调用失败");

    /** 状态码：200 表示成功，非 200 表示失败 */
    private final int code;

    /** 默认提示信息 */
    private final String msg;

    ResultCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
