-- 创建数据库
create database if not exists CodeBox;

use Codebox;

-- 题目表
create table if not exists question
(
    question_id    bigint primary key not null auto_increment comment '题目id',
    title          varchar(255)       not null comment '标题',
    content        text               not null comment '题目内容',
    description    text comment '题目描述',
    type           tinyint      default 0 comment '题目类型（0 acm赛制, 1 力扣赛制）',
    submit_count   bigint       default 0 comment '提交次数',
    accept_count   bigint       default 0 comment '通过次数',
    tags           varchar(255) default '' comment '题目标签',
    difficulty     tinyint      default 1 comment '难度 1-简单 2-中等 3-困难',
    author         varchar(255) default '' comment '创建人',
    memory         bigint       default 65535 comment '内存限制(KB),默认64MB',
    time_limit     int          default 1000 comment '时间限制(ms)',
    test_data_path varchar(255) default '' comment '测试数据目录地址(相对路径)',
    output_script  tinyint      default 0 comment '是否包含测试脚本 0-无、1-有',
    create_time    datetime     default current_timestamp comment '创建时间',
    update_time    datetime     default current_timestamp on update current_timestamp comment '更新时间',
    is_delete      tinyint      default 0 comment '是否删除 0-否 1-是',
    unique key (title) comment '标题索引'
) charset utf8mb4 comment '题目表';


-- 题目样例表
create table if not exists question_testCase
(
    id          bigint primary key not null auto_increment comment '样例id',
    question_id bigint             not null comment '题目id',
    case_input  text               not null comment '样例输入',
    case_output text               not null comment '样例输出',
    description text comment '样例解释',
    sort        int      default 0 comment '排序',
    create_time datetime default current_timestamp comment '创建时间',
    update_time datetime default current_timestamp on update current_timestamp comment '更新时间',
    index (question_id) comment '题目id索引'
) charset utf8mb4 comment '题目样例表';


-- 提交题目表
create table if not exists question_submit
(
    id          bigint primary key not null auto_increment comment '提交id',
    question_id bigint             not null comment '题目id',
    user_id     bigint             not null comment '提交用户id',
    language    varchar(32)        not null comment '编程语言',
    code        longtext           not null comment '提交代码',
    status      tinyint  default 0 comment '判题状态 0-排队中 1-答案正确 2-编译错误 3-超时 4-超内存 5-答案错误 6-未知错误  ',
    score       int      default 0 comment '总得分',
    time_used   float      default 0 comment '总耗时 ms',
    memory_used float      default 0 comment '总消耗内存 KB',
    error_msg   text comment '错误信息',
    submit_time datetime default current_timestamp comment '提交时间',
    index idx_userId_questionId (user_id, question_id) comment '先通过提交用户查找，在通过提交题目查找'
) charset utf8mb4 comment '题目提交表';