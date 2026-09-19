package com.coderank.constants;

/**
 * 系统配置常量类。
 */
public final class SystemConstants {

    private SystemConstants() {
    }

    // ==================== 对象存储 / 分片上传配置键 ====================

    /** 对象存储配置键。 */
    public static final String OBJECT_STORAGE = "ObjectStorage";

    /** 最终文件存储桶配置键。 */
    public static final String BUCKET_NAME = "BucketName";

    /** 分片临时存储桶配置键。 */
    public static final String TEMP_BUCKET_NAME_CONFIG_KEY = "FilePartUpload_Temp_Bucket_Name";

    /** 分片大小配置键。 */
    public static final String PART_SIZE_CONFIG_KEY = "FilePartUpload_File_Size";

    /** 分片上传任务过期秒数配置键。 */
    public static final String TASK_EXPIRE_SECONDS_CONFIG_KEY = "FilePartUpload_Task_Expire_Seconds";

    /** 单个用户分片上传并发数配置键。 */
    public static final String USER_CONCURRENCY_CONFIG_KEY = "FilePartUpload_User_Concurrency";

    // ==================== 发送邮件配置键 ====================

    /** SMTP 服务器地址配置键。 */
    public static final String MAIL_HOST = "MailHost";

    /** SMTP 服务器端口配置键。 */
    public static final String MAIL_PORT = "MailPort";

    /** 发件邮箱账号配置键。 */
    public static final String MAIL_USERNAME = "MailUsername";

    /** 发件邮箱授权码/密码配置键。 */
    public static final String MAIL_PASSWORD = "MailPassword";

    /** 发件人邮箱地址配置键。 */
    public static final String MAIL_FROM = "MailFrom";

    /** 发件人显示名称配置键。 */
    public static final String MAIL_FROM_NAME = "MailFromName";

    /** 是否启用 SSL 加密连接配置键。 */
    public static final String MAIL_SSL = "MailSsl";

    /** 是否启用 STARTTLS 配置键。 */
    public static final String MAIL_START_TLS = "MailStartTls";

    /** 是否启用 SMTP 认证配置键。 */
    public static final String MAIL_AUTH = "MailAuth";

    /** 邮件传输协议配置键。 */
    public static final String MAIL_PROTOCOL = "MailProtocol";

    /** 邮件内容编码配置键。 */
    public static final String MAIL_ENCODING = "MailEncoding";

    /** SMTP 连接超时时间(毫秒)配置键。 */
    public static final String MAIL_CONNECTION_TIMEOUT = "MailConnectionTimeout";

    /** SMTP 读写超时时间(毫秒)配置键。 */
    public static final String MAIL_TIMEOUT = "MailTimeout";
}
