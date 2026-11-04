package com.coderank.exception;

import lombok.Getter;

/**
 * 业务异常。
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 默认业务错误码 */
    private static final int DEFAULT_CODE = 500;

    /** 业务错误码 */
    private final int code;

    public BusinessException(String msg) {
        super(msg);
        this.code = DEFAULT_CODE;
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BusinessException(String msg, Throwable cause) {
        super(msg, cause);
        this.code = DEFAULT_CODE;
    }

    public BusinessException(int code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
    }
}
