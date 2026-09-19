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
    tags          json                        default null comment '题目标签',
    template      json                        default null comment '代码模板：按语言分别存放',
    sample_case   json                        default null comment '题目样例，仅用于展示',
    source        varchar(128)                default null comment '题目来源',
    memory        bigint             not null default 65535 comment '内存限制(KB)，默认64MB',
    stack_limit   bigint             not null default 8192 comment '栈空间限制(KB)，默认8MB',
    time_limit    int                not null default 1000 comment '时间限制(ms)',
    output_script tinyint            not null default 0 comment '是否包含测试脚本：0无，1有',
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


