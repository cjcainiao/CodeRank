package com.coderank.exception;

import com.coderank.enums.ErrorCode;
import com.coderank.result.Result;
import lombok.extern.java.Log;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.logging.Level;

/**
 * <p>
 * 全局异常处理。
 * </p>
 *
 * <p>日志使用 Lombok {@link Log} 注解（基于 JDK 自带的 {@code java.util.logging}），
 * 因此 common 模块无需依赖 slf4j-api。</p>
 *
 * <p>仅 Servlet Web 应用（如 auth）生效；WebFlux 应用（如 gateway）不加载本类，
 * 避免 gateway 因缺少 spring-webmvc 而启动失败。</p>
 */
@Log
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@RestControllerAdvice
public class GlobalException {

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.log(Level.WARNING, "业务异常：code={0}, message={1}",
                new Object[]{e.getCode(), e.getMessage()});
        // 业务异常通常包裹了真实根因（如 MinIO 连接失败），一并输出堆栈便于排查
        if (e.getCause() != null) {
            log.log(Level.WARNING, "业务异常根因：", e);
        }

        // 参数、认证等客户端可处理的错误保留明确提示；服务器内部错误只返回标准文案，
        // 避免 SQL、Redis、对象存储等底层异常信息泄露给前端。
        boolean serverError = e.getCode() >= ErrorCode.BUSINESS_ERROR.getCode();
        int responseCode = serverError ? ErrorCode.SYSTEM_ERROR.getCode() : e.getCode();
        String responseMessage = serverError
                ? ErrorCode.SYSTEM_ERROR.getMessage()
                : e.getMessage();
        return buildResponse(HttpStatus.OK, responseCode, responseMessage, null);
    }

    /** 方法参数校验异常（@RequestBody 参数） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = firstErrorMessage(e.getBindingResult().getFieldErrors());
        log.log(Level.WARNING, "参数校验失败：{0}", message);
        return buildResponse(HttpStatus.OK, ErrorCode.BAD_REQUEST.getCode(), message, null);
    }

    /** 表单绑定校验异常（@ModelAttribute 参数） */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException e) {
        String message = firstErrorMessage(e.getBindingResult().getFieldErrors());
        log.log(Level.WARNING, "参数绑定失败：{0}", message);
        return buildResponse(HttpStatus.OK, ErrorCode.BAD_REQUEST.getCode(), message, null);
    }

    /** 缺少必填请求参数（@RequestParam required 未传） */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParameter(MissingServletRequestParameterException e) {
        String message = "缺少必填参数：" + e.getParameterName();
        log.log(Level.WARNING, "缺少必填参数：{0}", e.getParameterName());
        return buildResponse(HttpStatus.OK, ErrorCode.BAD_REQUEST.getCode(), message, null);
    }

    /** 参数类型不匹配 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String message = "参数类型错误：" + e.getName();
        log.warning(message);
        return buildResponse(HttpStatus.OK, ErrorCode.BAD_REQUEST.getCode(), message, null);
    }

    /** 请求体不可读（JSON 解析失败等） */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        log.log(Level.WARNING, "请求体解析失败：{0}", e.getMessage());
        return buildResponse(HttpStatus.OK, ErrorCode.BAD_REQUEST.getCode(), "请求体格式错误", null);
    }

    /** 上传文件大小超过限制 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<Void>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e) {
        log.log(Level.WARNING, "上传文件大小超过限制：maxUploadSize={0}", e.getMaxUploadSize());
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE,
                ErrorCode.FILE_SIZE_EXCEEDED.getCode(),
                ErrorCode.FILE_SIZE_EXCEEDED.getMessage(), null);
    }

    /** 资源/接口不存在（访问不存在的路径）——返回 404，而非系统异常 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFound(NoResourceFoundException e) {
        log.log(Level.WARNING, "访问的资源不存在：{0}", e.getResourcePath());
        return buildResponse(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND.getCode(),
                ErrorCode.NOT_FOUND.getMessage(), null);
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.log(Level.SEVERE, "系统异常", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.SYSTEM_ERROR.getCode(),
                ErrorCode.SYSTEM_ERROR.getMessage(), null);
    }

    private String firstErrorMessage(java.util.List<FieldError> fieldErrors) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return ErrorCode.BAD_REQUEST.getMessage();
        }
        return fieldErrors.get(0).getDefaultMessage();
    }

    private <T> ResponseEntity<Result<T>> buildResponse(HttpStatus httpStatus, int code, String message, T data) {
        return ResponseEntity.status(httpStatus).body(Result.error(code, message, data));
    }
}
