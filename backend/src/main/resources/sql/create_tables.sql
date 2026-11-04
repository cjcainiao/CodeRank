-- 数据库初始化脚本

drop database if exists coderank;

create database coderank;

\c coderank

-- 用户表
create table "user"
(
    id              bigint primary key,                                   -- 主键，雪花算法生成
    username        varchar(50)           not null,                       -- 登录账号
    password        varchar(100)          not null,                       -- 登录密码，存加密后的密文
    nickname        varchar(50),                                          -- 昵称
    email           varchar(100),                                         -- 邮箱
    phone           varchar(20),                                          -- 手机号
    avatar          varchar(255),                                         -- 头像地址
    signature       varchar(255),                                         -- 个性签名
    gender          smallint     default 0 not null,                      -- 性别：0 未知，1 男，2 女
    role            smallint     default 0 not null,                      -- 角色：0 普通用户，1 管理员
    status          smallint     default 0 not null,                      -- 账号状态：0 正常，1 禁用
    last_login_time timestamp,                                            -- 最后登录时间
    create_time     timestamp    default current_timestamp not null,      -- 创建时间
    update_time     timestamp    default current_timestamp not null       -- 更新时间
);

-- 登录账号唯一
create unique index uk_user_username on "user" (username);

-- 邮箱唯一，未填邮箱的记录不参与唯一约束
create unique index uk_user_email on "user" (email) where email is not null;
