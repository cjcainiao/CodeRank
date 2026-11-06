package com.coderank.exception;

import com.coderank.common.enums.ResultCodeEnum;
import lombok.Getter;

/**
 * 业务异常。
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务错误码 */
    private final int code;

    public BusinessException(String msg) {
        super(msg);
        this.code = ResultCodeEnum.BUSINESS_ERROR.getCode();
    }

    public BusinessException(ResultCodeEnum resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCodeEnum resultCode, String msg) {
        super(msg);
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCodeEnum resultCode, String msg, Throwable cause) {
        super(msg, cause);
        this.code = resultCode.getCode();
    }
}
