package com.coderank.exception;


import com.coderank.enums.ResultCode;
import com.coderank.utils.ResponseResult;
import org.apache.ibatis.ognl.MethodFailedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * 运行时异常处理（最后兜底）
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseResult handleRuntimeException(RuntimeException e) {
        return ResponseResult.error("服务器出现错误！");
    }


    /**
     * 业务异常处理
     * @param e
     * @return
     */

    @ExceptionHandler(BusinessException.class)
    public ResponseResult handleBusinessException(BusinessException e) {
        return ResponseResult.error(e.getCode(),e.getMessage());
    }


    /**
     * 参数绑定错误
     * @return
     */
    @ExceptionHandler(BindException.class)
    public ResponseResult RequestParameterError(){
        return ResponseResult.error(ResultCode.PARAMETER_ERROR.getCode(), ResultCode.PARAMETER_ERROR.getMsg());
    }

    /**
     * 方法参数错误
     * @return
     */
    @ExceptionHandler(MethodFailedException.class)
    public ResponseResult MethodParameterError(){
        return ResponseResult.error(ResultCode.PARAMETER_ERROR.getCode(), ResultCode.PARAMETER_ERROR.getMsg());
    }
}