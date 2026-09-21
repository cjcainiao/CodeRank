-- 数据库初始化脚本

drop
database if exists `coderank`;

create
database `coderank`
    default character set utf8mb4
    default collate utf8mb4_unicode_ci;

use
`coderank`;


-- 创建表结构

-- 用户表
create table `user`
(
    user_id         bigint primary key not null auto_increment comment '用户id',
    username        varchar(50)        not null comment '用户名',
    password        varchar(512)       not null comment '密码',
    age             int                         default null comment '年龄',
    sex             int                not null default 0 comment '性别：0未知，1男，2女',
    nickname        varchar(50)                 default null comment '昵称',
    avatar          varchar(512)                default null comment '头像地址',
    email           varchar(128)                default null comment '邮箱',
    phone           varchar(20)                 default null comment '手机号码',
    profile         varchar(512)                default null comment '用户简介',
    status          tinyint            not null default 1 comment '账号状态：0禁用，1正常',
    last_login_time datetime                    default null comment '最后登录时间',
    last_login_ip   varchar(64)                 default null comment '最后登录IP',
    create_time     datetime           not null default current_timestamp comment '创建时间',
    update_time     datetime           not null default current_timestamp on update current_timestamp comment '更新时间',
    unique key `uk_user_username` (`username`),
    key             `idx_user_email` (`email`),
    key             `idx_user_phone` (`phone`),
    key             `idx_user_nickname` (`nickname`),
    key             `idx_user_status_sex_create_time` (`status`, `sex`, `create_time`),
    key             `idx_user_last_login_time` (`last_login_time`)
) engine = innodb default charset = utf8mb4 comment = '用户表';


-- 题目表
create table `question`
(
    question_id   bigint primary key not null auto_increment comment '题目id',
    title         varchar(255)       not null comment '题目标题',
    content       text               not null comment '题目内容',
    description   text                        default null comment '题目描述',
    difficulty    tinyint            not null default 1 comment '难度：1简单，2中等，3困难',
    type          tinyint            not null default 0 comment '题目形态：0acm赛制，1力扣赛制',
    judge_mode    tinyint            not null default 0 comment '判题模式：0严格比对，1特判，2交互',
    sample_case   json                        default null comment '题目样例，仅用于展示',
    source        varchar(128)                default null comment '题目来源',
    memory        bigint             not null default 65535 comment '内存限制(KB)，默认64MB',
    stack_limit   bigint             not null default 8192 comment '栈空间限制(KB)，默认8MB',
    time_limit    int                not null default 1000 comment '时间限制(ms)',
    output_script tinyint            not null default 0 comment '是否包含测试脚本：0无，1有',
    output_script_content text                default null comment '输出结果处理脚本内容',
    submit_count  bigint             not null default 0 comment '提交次数',
    accept_count  bigint             not null default 0 comment '通过次数',
    author_id     bigint             not null comment '创建人用户id',
    status        tinyint            not null default 1 comment '题目状态：0下架，1上架',
    create_time   datetime           not null default current_timestamp comment '创建时间',
    update_time   datetime           not null default current_timestamp on update current_timestamp comment '更新时间',
    unique key `uk_question_title` (`title`),
    key           `idx_question_status_judge_mode` (`status`, `judge_mode`),
    key           `idx_question_difficulty_type` (`difficulty`, `type`),
    key           `idx_question_create_time` (`create_time`),
    key           `idx_question_author_id` (`author_id`)
) engine = innodb default charset = utf8mb4 comment = '题目表';


-- 答案文件存放表
create table `question_answer_file`
(
    answer_file_id bigint primary key not null auto_increment comment '答案文件id',
    question_id    bigint             not null comment '所属题目id',
    case_no        int                not null comment '测试点序号',
    bucket_name    varchar(128)       not null comment 'MinIO存储桶名称',
    input_file_path  varchar(512)     not null comment '输入数据文件存放路径',
    output_file_path varchar(512)     not null comment '正确答案文件存放路径',
    input_file_hash  varchar(128)              default null comment '输入文件内容哈希',
    output_file_hash varchar(128)              default null comment '正确答案文件内容哈希',
    status         tinyint            not null default 1 comment '文件状态：0停用，1启用',
    uploader_id    bigint             not null comment '上传人用户id',
    create_time    datetime           not null default current_timestamp comment '创建时间',
    update_time    datetime           not null default current_timestamp on update current_timestamp comment '更新时间',
    unique key `uk_answer_file_question_case` (`question_id`, `case_no`),
    key            `idx_answer_file_question_status` (`question_id`, `status`),
    key            `idx_answer_file_uploader_id` (`uploader_id`)
) engine = innodb default charset = utf8mb4 comment = '题目答案文件存放表';


-- 提交任务表
create table `submission_task`
(
    submission_id     bigint primary key not null auto_increment comment '提交任务id',
    task_type         tinyint            not null comment '任务类型：0测试运行，1正式评测',
    question_id       bigint                      default null comment '题目id',
    submitter_id      bigint             not null comment '提交人用户id',
    language          varchar(32)        not null comment '编程语言',
    source_code       text               not null comment '提交代码',
    code_template     text                        comment '代码模板',
    input_data        text                        comment '自定义输入数据，测试运行使用',
    status            tinyint            not null default 0 comment '任务状态：0等待，1运行中，2已完成，3失败',
    judge_result      tinyint                     default null comment '判题结果：1通过，2答案错误，3超时，4内存超限，5运行错误，6编译错误',
    output_data       text                        comment '程序输出',
    error_message     text                        comment '编译或运行错误信息',
    time_used         int                         default null comment '执行耗时（ms）',
    memory_used       bigint                      default null comment '内存使用量（KB）',
    judge_message_list json                        comment '判题信息列表',
    create_time       datetime           not null default current_timestamp comment '创建时间',
    update_time       datetime           not null default current_timestamp on update current_timestamp comment '更新时间',
    key `idx_submission_submitter_id` (`submitter_id`),
    key `idx_submission_question_id` (`question_id`),
    key `idx_submission_type_status` (`task_type`, `status`)
) engine = innodb default charset = utf8mb4 comment = '代码提交与评测任务表';
