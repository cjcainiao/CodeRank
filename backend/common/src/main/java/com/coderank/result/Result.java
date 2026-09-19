package com.coderank.result;

import com.coderank.enums.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 全局统一响应
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码：0 表示成功，非 0 表示失败 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 返回数据，可为空 */
    private T data;

    private Result() {
    }

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功，无数据 */
    public static <T> Result<T> success() {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
    }

    /** 成功，携带数据 */
    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    /** 成功，携带数据与自定义提示 */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), message, data);
    }

    /** 失败，使用错误码枚举 */
    public static <T> Result<T> error(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /** 失败，使用错误码枚举 + 自定义提示 */
    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }

    /** 失败，自定义错误码与提示 */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /** 失败，携带错误对象 */
    public static <T> Result<T> error(int code, String message, T data) {
        return new Result<>(code, message, data);
    }
}
