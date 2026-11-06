package com.coderank.common.result;

import com.coderank.common.enums.ResultCodeEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应体。
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码：200 表示成功，非 200 表示失败 */
    private int code;

    /** 提示信息 */
    private String msg;

    /** 响应数据 */
    private T data;

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功，无数据
    public static <T> Result<T> success() {
        return new Result<>(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMsg(), null);
    }

    // 成功，携带数据
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMsg(), data);
    }

    // 成功，自定义提示
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCodeEnum.SUCCESS.getCode(), msg, data);
    }

    // 失败，使用系统异常码
    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCodeEnum.SYSTEM_ERROR.getCode(), msg, null);
    }

    // 失败，指定响应码
    public static <T> Result<T> error(ResultCodeEnum resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    // 失败，指定响应码并覆盖提示
    public static <T> Result<T> error(ResultCodeEnum resultCode, String msg) {
        return new Result<>(resultCode.getCode(), msg, null);
    }

    // 失败，直接指定状态码与提示，供异常处理器透传业务码
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
