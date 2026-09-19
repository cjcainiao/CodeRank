package com.coderank.enums;

import lombok.Getter;

/**
 * <p>
 * 统一错误码枚举。
 * </p>
 *
 * <p>用于全局异常处理和统一响应体，规范业务侧与系统侧的返回码。</p>
 *
 * @author cainiao
 */
@Getter
public enum ErrorCode {

    /** 成功 */
    SUCCESS(0, "操作成功"),

    /** 请求参数错误 */
    BAD_REQUEST(40000, "请求参数错误"),

    /** 未认证 / 未登录 */
    UNAUTHORIZED(40100, "未登录或登录已过期"),

    /** 当前用户未登录 */
    NOT_LOGIN(40101, "用户未登录"),

    /** 无权限 */
    FORBIDDEN(40300, "没有访问权限"),

    /** 资源不存在 */
    NOT_FOUND(40400, "请求的资源不存在"),

    /** 请求方法不支持 */
    METHOD_NOT_ALLOWED(40500, "请求方法不支持"),

    /** 业务处理失败（通用业务异常） */
    BUSINESS_ERROR(50000, "业务处理失败"),

    /** 系统内部错误 */
    SYSTEM_ERROR(50001, "服务器异常"),

    /** 数据库异常 */
    DB_ERROR(50002, "数据库操作异常"),

    /** 远程调用异常 */
    REMOTE_ERROR(50003, "远程服务调用失败"),

    /** 文件上传失败 */
    FILE_UPLOAD_FAILED(51000, "文件上传失败"),

    /** 文件下载失败 */
    FILE_DOWNLOAD_FAILED(51001, "文件下载失败"),

    /** 文件下载地址生成失败 */
    FILE_DOWNLOAD_URL_GENERATION_FAILED(51002, "文件下载地址生成失败"),

    /** 文件查询失败 */
    FILE_QUERY_FAILED(51003, "文件查询失败"),

    /** 文件分片合并失败 */
    FILE_MERGE_FAILED(51004, "文件分片合并失败"),

    /** 文件删除失败 */
    FILE_DELETE_FAILED(51005, "文件删除失败"),

    /** 文件访问地址生成失败 */
    FILE_ACCESS_URL_GENERATION_FAILED(51006, "文件访问地址生成失败"),

    /** 上传文件大小超过限制 */
    FILE_SIZE_EXCEEDED(51007, "上传文件大小超过限制"),

    /** 文件分片上传任务不存在 */
    FILE_UPLOAD_TASK_NOT_FOUND(51008, "文件分片上传任务不存在"),

    /** 文件分片上传初始化失败 */
    FILE_MULTIPART_UPLOAD_INIT_FAILED(51009, "文件分片上传初始化失败"),

    /** 文件指纹计算失败 */
    FILE_HASH_CALCULATION_FAILED(51010, "文件指纹计算失败"),

    /** 当前文件分片已存在 */
    FILE_UPLOAD_PART_ALREADY_EXISTS(51011, "当前文件分片已存在"),

    /** 文件分片校验失败 */
    FILE_UPLOAD_PART_VALIDATION_FAILED(51012, "文件分片校验失败"),

    /** Redis 操作失败 */
    REDIS_OPERATION_FAILED(52000, "Redis 操作失败"),

    /** Redis 数据转换失败 */
    REDIS_SERIALIZATION_FAILED(52001, "Redis 数据转换失败"),

    /** 系统配置项不存在 */
    CONFIG_NOT_FOUND(53000, "系统配置项不存在"),

    /** Spring Bean 不存在 */
    BEAN_NOT_FOUND(53001, "未找到对应的 Bean");

    private final int code;

    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误码查询对应的枚举，未匹配时返回 {@link #SYSTEM_ERROR}。
     */
    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return SYSTEM_ERROR;
    }
}
