-- 数据库初始化脚本
-- 执行前请显式连接到 coderank 库，脚本不负责建库和切换数据库

-- 用户表
create table if not exists "user"
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
create unique index if not exists uk_user_username on "user" (username);

-- 邮箱唯一，未填邮箱的记录不参与唯一约束
create unique index if not exists uk_user_email on "user" (email) where email is not null;

-- 题目表
create table if not exists question
(
    id              bigint primary key,                                   -- 主键，雪花算法生成
    title           varchar(200)          not null,                       -- 题目标题
    difficulty      smallint     default 1 not null,                      -- 难度：1 简单，2 中等，3 困难
    judge_mode      smallint     default 1 not null,                      -- 判题模式：1 标准输入输出，2 核心代码
    description     text                 not null,                        -- 题目描述，Markdown
    input_format    text,                                                 -- 输入格式说明，Markdown，标准输入输出模式用
    output_format   text,                                                 -- 输出格式说明，Markdown，标准输入输出模式用
    sample_cases    jsonb        default '[]' not null,                   -- 样例，[{"input":"","output":""}]
    hint            text,                                                 -- 提示，Markdown
    time_limit      integer      default 1000 not null,                   -- 时间限制，毫秒
    memory_limit    integer      default 128 not null,                    -- 内存限制，MB
    source          varchar(100),                                         -- 题目来源
    status          smallint     default 0 not null,                      -- 状态：0 草稿，1 已发布，2 已下架
    submit_count    integer      default 0 not null,                      -- 提交次数
    accepted_count  integer      default 0 not null,                      -- 通过次数
    create_time     timestamp    default current_timestamp not null,      -- 创建时间
    update_time     timestamp    default current_timestamp not null       -- 更新时间
);

-- 用户端只查已发布的题目，状态是最常用的过滤条件
create index if not exists idx_question_status on question (status);

-- 按难度筛选
create index if not exists idx_question_difficulty on question (difficulty);

-- 按判题模式筛选
create index if not exists idx_question_judge_mode on question (judge_mode);

-- 列表默认按创建时间倒序
create index if not exists idx_question_create_time on question (create_time desc);

-- 样例为 jsonb，供内容检索
create index if not exists idx_question_sample_cases on question using gin (sample_cases);

-- 标签表
create table if not exists tag
(
    id              bigint primary key,                                   -- 主键，雪花算法生成
    name            varchar(50)           not null,                       -- 标签名，如二分、动态规划
    color           varchar(20),                                          -- 标签颜色，十六进制色值
    sort_order      integer      default 0 not null,                      -- 排序值，越小越靠前
    create_time     timestamp    default current_timestamp not null,      -- 创建时间
    update_time     timestamp    default current_timestamp not null       -- 更新时间
);

-- 标签名唯一且不区分大小写，避免「二分」「二分 」重复录入
create unique index if not exists uk_tag_name on tag (lower(name));

-- 题目标签关联表
create table if not exists question_tag
(
    question_id     bigint not null,                                      -- 题目主键
    tag_id          bigint not null,                                      -- 标签主键
    create_time     timestamp default current_timestamp not null,         -- 创建时间
    update_time     timestamp default current_timestamp not null,         -- 更新时间
    primary key (question_id, tag_id)
);

-- 按标签反查题目
create index if not exists idx_question_tag_tag_id on question_tag (tag_id);

-- 题目代码模板表
create table if not exists question_code_template
(
    id              bigint primary key,                                   -- 主键，雪花算法生成
    question_id     bigint                 not null,                      -- 所属题目
    language        smallint               not null,                      -- 语言：1 Java，2 C++，3 Python，4 Go，5 JavaScript
    template        text                   not null,                      -- 初始代码
    create_time     timestamp    default current_timestamp not null,      -- 创建时间
    update_time     timestamp    default current_timestamp not null       -- 更新时间
);

-- 一道题每种语言只有一份模板，同时支撑按题目查模板
create unique index if not exists uk_question_code_template on question_code_template (question_id, language);

-- 题目测试用例表
create table if not exists question_test_case
(
    id              bigint primary key,                                   -- 主键，雪花算法生成
    question_id     bigint                 not null,                      -- 所属题目
    args            jsonb        default '[]' not null,                   -- 函数入参，核心代码模式用，如 [[2,7,11,15],9]
    expected        jsonb,                                                -- 期望返回值，核心代码模式用
    input_data      text,                                                 -- 标准输入，标准输入输出模式用
    expected_out    text,                                                 -- 标准输出，标准输入输出模式用
    visible         smallint     default 1 not null,                      -- 是否展示：0 隐藏，1 展示
    sort_order      integer      default 0 not null,                      -- 排序值，越小越靠前
    create_time     timestamp    default current_timestamp not null,      -- 创建时间
    update_time     timestamp    default current_timestamp not null       -- 更新时间
);

-- 按题目取用例，列表默认按排序值升序
create index if not exists idx_question_test_case_question on question_test_case (question_id, sort_order);

-- 题目评测数据表
create table if not exists question_test_data
(
    id              bigint primary key,                                   -- 主键，雪花算法生成
    question_id     bigint                 not null,                      -- 所属题目
    case_no         integer                not null,                      -- 测试点编号，从 1 开始，对应 1.in 与 1.out
    input_size      bigint       default 0 not null,                      -- 输入文件字节数
    output_size     bigint       default 0 not null,                      -- 输出文件字节数
    visible         smallint     default 0 not null,                      -- 是否展示：0 隐藏，1 展示
    create_time     timestamp    default current_timestamp not null,      -- 创建时间
    update_time     timestamp    default current_timestamp not null       -- 更新时间
);

-- 同一道题内测试点编号唯一，同时支撑按题目查全部测试点
create unique index if not exists uk_question_test_data on question_test_data (question_id, case_no);
