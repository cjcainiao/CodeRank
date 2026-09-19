package com.coderank.exception;

import com.coderank.enums.ErrorCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ErrorCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
