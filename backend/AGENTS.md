# CodeRank 后端开发规范

本文件约束 `backend/` 下的开发方式与注释写法。新增或修改代码时须遵循。

## 技术栈

| 组件 | 版本 |
|---|---|
| Spring Boot | 3.5.16 |
| Java | 17 |
| Maven | 3.9+ |
| MyBatis-Plus | 3.5.18 |
| PostgreSQL | 12+ |

版本升级须同步更新本表。

密码加密用 `spring-security-crypto` 的 BCrypt，参数校验用 `spring-boot-starter-validation`，两者版本由 Spring Boot 父 POM 管理，不写版本号。

## 依赖管理

- 第三方依赖的版本号**直接写在 `<dependency>` 上**，不使用 `<dependencyManagement>`。
- Spring Boot 父 POM 已管理的依赖（`lombok`、`spring-boot-starter-test` 等）不写版本号。

## 数据库

- 数据库统一使用 **PostgreSQL**，驱动 `org.postgresql:postgresql`。
- 主键用 `bigint`，由雪花算法生成（`@TableId(type = IdType.ASSIGN_ID)`），不用 `serial` 和自增。
- 布尔语义用 `smallint` + 注释说明取值，不用 `boolean`。枚举值在注释中列全。
- 结构化 JSON 用 **`jsonb`**，并为查询字段建 `gin` 索引。
- 表名、字段名不加引号，保留字（如 `user`）加双引号。
- 建表脚本放 `resources/sql/create_tables.sql`，字段说明用行尾 `--` 注释，不写 `comment on` 语句。
- 建表与建索引统一带 `if not exists`，脚本可重复执行。
- 删除一律用物理删除，不加 `deleted` 标记字段，也不配 `@TableLogic`。
- 关联表用复合主键（如 `question_tag` 的 `(question_id, tag_id)`），不另设自增主键；需要反向查询时单独建索引。

## 目录结构

```
backend/
├── pom.xml
├── .env                               # 本地环境变量（不入库）
├── .env.example                       # 环境变量模板（入库）
└── src/main/
    ├── java/com/coderank/
    │   ├── CodeRankApplication.java   # 启动类
    │   ├── common/
    │   │   ├── config/                # 公共配置类
    │   │   ├── result/                # 统一响应体
    │   │   └── utils/                 # 通用工具类
    │   ├── controller/
    │   │   ├── backend/               # 后台管理接口
    │   │   │   └── question/          # 后台题目相关接口
    │   │   └── home/                  # 首页用户端接口
    │   ├── entity/
    │   │   ├── dto/                   # 请求数据传输对象
    │   │   │   ├── question/          # 题目、标签相关
    │   │   │   ├── judge/             # 评测机相关
    │   │   │   └── user/              # 用户相关
    │   │   ├── pojo/                  # 数据库实体
    │   │   └── vo/                    # 响应视图对象
    │   │       ├── question/          # 题目、标签相关
    │   │       ├── judge/             # 评测机相关
    │   │       └── user/              # 用户相关
    │   ├── exception/                 # 业务异常与全局异常处理
    │   ├── handler/                   # MyBatis 类型处理器
    │   ├── mapper/
    │   │   ├── backend/               # 后台管理数据访问
    │   │   └── home/                  # 首页用户端数据访问
    │   ├── properties/                # 配置绑定类
    │   └── service/
    │       ├── backend/
    │       │   └── impl/              # 后台管理业务实现
    │       └── home/
    │           └── impl/              # 首页用户端业务实现
    └── resources/
        ├── application.yml            # 通用配置
        ├── application-dev.yml        # 开发环境
        ├── application-prod.yml       # 生产环境
        ├── mapper/                    # MyBatis XML 映射文件
        └── sql/                       # 建表脚本
```

新增包须与本结构保持一致，不随意平铺在 `com.coderank` 根下。

### 分组规则

- `controller`、`mapper`、`service` 按调用端分组：后台管理放 `backend/` 子包，用户端放 `home/` 子包，接口路径前缀同名（`/backend/questions`、`/home/questions`）。
- `entity` 下的 `dto`、`vo` 按**业务域**分组：`question/`、`judge/`、`user/`，与 `controller/backend/` 的业务子包对应，便于按功能定位文件。
- `pojo` 不分组，与表一一对应，保持扁平。
- 包名与接口路径前缀须一致，便于按 URL 反查代码。


## 分层职责

- **controller**：只做参数接收、调用 service、返回结果。不写业务逻辑、不直接调 mapper。参数校验用 `@Valid` 加 `jakarta.validation` 注解，校验规则写在 DTO 上。
- **service**：承载业务逻辑。对外暴露接口，实现类放同组的 `impl/` 下（如 `service/backend/impl/`）。存在性校验、唯一性校验、默认值填充、密码加密都在这一层，不放到 controller 或 mapper。
- **mapper**：只定义数据访问方法。接口在 `java/com/coderank/mapper/<端>/`，XML 在 `resources/mapper/`，两者同名对应。Mapper 接口继承 `BaseMapper<T>`，加 `@Mapper` 注解。

## MyBatis-Plus

- 单表增删改查一律用 `BaseMapper` 提供的方法，Mapper 接口留空即可，不写重复的 XML。
- 条件查询用 `LambdaQueryWrapper`，不手拼字符串，避免字段名写错。
- 分页用 `selectPage` 加 `Page`，`Page` 的页码从 1 开始；`PaginationInnerInterceptor` 已在 `MybatisPlusConfig` 注册，新增数据源时按数据库类型配置 `DbType`。
- 删除用 `deleteById`，直接物理删除，不引入 `@TableLogic`。
- 创建时间、更新时间标 `@TableField(fill = ...)`，由 `MyMetaObjectHandler` 自动填充，不在业务代码里手动 `set`。
- 表名与 Java 关键字冲突时用 `@TableName("\"user\"")` 转义，与建表脚本的双引号保持一致。
- 分页每页条数须在 service 层设上限（如 `MAX_PAGE_SIZE = 100`），防止前端传超大值拖垮数据库。

### jsonb 字段映射

- `jsonb` 列在实体里映射为 `String`，并标 `@TableField(typeHandler = JsonbTypeHandler.class)`。
- 实体类必须同时加 `@TableName(value = "表名", autoResultMap = true)`，否则类型处理器只在写入时生效，查询会失败。
- `JsonbTypeHandler` 放在 `com.coderank.handler`，写入时用 `ps.setObject(i, parameter, Types.OTHER)` 声明 JDBC 类型。
- 入参为 JSON 的字段在 service 层用 `ObjectMapper` 校验格式，非法时抛 `BusinessException(400, ...)`，避免脏数据写进 `jsonb` 列。

## 级联与事务

- 题目删除时须在同一事务内清理其从属数据：标签关联、代码模板、测试用例、评测数据。
- 主表与从属表的写操作加 `@Transactional(rollbackFor = Exception.class)`，保证要么全部成功要么全部回滚。
- 批量写入从属数据时逐个处理并收集失败项，单个失败不阻断其余数据，最后统一提示。

## 实体分层

- **pojo**：与数据库表一一对应，字段名转小驼峰，不加业务字段。
- **dto**：接收请求参数，命名 `XxxDTO`，只放接口需要的字段。校验注解写在这里。
- **vo**：返回给前端的视图数据，命名 `XxxVO`，不放敏感字段（如密码）。须实现 `Serializable` 并声明 `private static final long serialVersionUID = 1L;`。
- `pojo`、`dto` 不强制实现 `Serializable`；若实体要写入 Redis 缓存，再补上。
- MyBatis-Plus 的 `type-aliases-package` 只配 `com.coderank.entity.pojo`。

## 统一响应

- 所有接口返回 `Result<T>`（`com.coderank.common.result`），不直接返回裸对象。
- 成功用 `Result.success(data)`，失败用 `Result.error(msg)` 或 `Result.error(code, msg)`。
- `code` 为 `0` 表示成功，非 `0` 表示失败。
- 分页数据统一返回 `PageResult<T>`（`com.coderank.common.result`），由 `PageResult.of(page, mapper)` 转换，字段为 `records`、`total`、`pageNum`、`pageSize`、`pages`。
- 列表数据（不分页）直接返回 `List<XxxVO>`。
- 无返回值的接口用 `Result<Void>` 加 `Result.success()`。
- 新增接口的主键统一转字符串返回 `Result<String>`，避免雪花 ID 在前端丢精度。
- Controller 不写 try-catch 吞异常，直接抛出让全局处理器接管。

## 异常处理

- 业务失败一律抛 `BusinessException`，不带堆栈的用 `new BusinessException("提示")`，需指定错误码用 `new BusinessException(1001, "提示")`。
- 错误码沿用 HTTP 语义：`400` 参数问题、`404` 资源不存在、`409` 唯一性冲突；业务域错误码从 `1001` 起编号。
- 参数校验类异常由 `GlobalExceptionHandler` 统一转 400 码，无需手写校验返回。
- 兜底异常返回通用文案，**不把堆栈或 SQL 信息暴露给前端**，细节写日志。

## 命名

- 类名大驼峰：`QuestionController`、`QuestionService`、`QuestionMapper`。
- 接口与实现：接口 `QuestionService`，实现 `QuestionServiceImpl`。
- 方法名小驼峰动词开头：`getQuestionById`、`createQuestion`、`deleteById`。
- 常量全大写下划线分割：`MAX_CODE_LENGTH`、`DEFAULT_PORT`。
- 包名全小写，不用下划线。
- DTO 按动作命名：`XxxCreateDTO`、`XxxUpdateDTO`、`XxxQueryDTO`，不复用同一个 DTO 承载多种请求。
- 分页查询参数类统一以 `QueryDTO` 结尾，含 `pageNum`、`pageSize` 与筛选字段。

## 接口设计

- 路径用复数名词、全小写中划线分隔：`/backend/users`、`/backend/questions`。
- 从属资源用「父路径 + 子路径」表达，如 `/backend/question-test-cases/question/{questionId}` 表示按题目查其用例。
- 请求方法对应语义：`GET` 查询、`POST` 新增、`PUT` 全量修改、`PATCH` 局部修改、`DELETE` 删除。
- 路径变量用 `{id}`，方法参数用 `@PathVariable`；查询条件用 `@ModelAttribute` 自动绑定 `QueryDTO`，不加 `@RequestParam` 逐个接。
- 新增返回主键 `Result<String>`（雪花 ID 转字符串），修改、删除返回 `Result<Void>`。
- 不暴露实体的全部字段给前端，一律经 VO 输出。
- 修改接口中，字段为 `null` 表示不修改该字段；需要「清空」语义时用空集合或空字符串，由 service 层区分。

## 安全

- 密码只以 BCrypt 密文入库，`BCryptPasswordEncoder` 实例在 service 内维护，不放到 controller。
- 任何 VO 不得包含 `password` 字段，实体转 VO 用 `BeanUtils.copyProperties`，靠 VO 无该字段自然过滤。
- 手机号、邮箱等敏感字段若后续需要脱敏，在 VO 组装阶段处理，不改数据库存储。

## 构建验证

- 改动代码后至少执行一次 `mvn compile`，确认无编译错误再提交。
- 本机 `JAVA_HOME` 须指向 JDK 根目录（如 `C:\Program Files\Java\jdk17`），不能带 `\bin` 后缀，否则 Maven 启动报 `JAVA_HOME is not defined correctly`。
- 项目未带 `mvnw`，Maven 通过 IDEA 内置或本地安装的发行版调用。

## 配置文件

- 通用项写 `application.yml`，环境相关项下沉到 `application-{profile}.yml`。
- 当前激活 profile 在 `application.yml` 的 `spring.profiles.active` 指定，默认 `dev`。
- 生产环境用启动参数覆盖，不改文件：
  ```
  java -jar app.jar --spring.profiles.active=prod
  ```
- 密码、密钥等敏感信息走环境变量占位 `${DB_PASSWORD}`，**不写明文**。
- 环境相关的路径同理走环境变量，如上传根目录 `${FILE_UPLOAD_ROOT:./data}`，`dev` 用项目内目录，`prod` 用数据盘路径。

### 文件上传

- 大小限制分两层：`spring.servlet.multipart` 是框架层请求限制，业务上限写在各模块自己的配置项里，两者须保持一致。
- 上传根目录统一由 `file.upload.root` 控制，评测数据在其下按 `testdata/{questionId}/{caseNo}.in` 组织。
- 允许的扩展名用白名单配置，如 `file.upload.allowed-extensions: in,out`，不做黑名单。
- 文件路径不存数据库，由 `question_id`、`case_no` 和配置的根目录推算，换存储位置只改配置。

## 代码风格

- 缩进 **4 空格**，不使用 Tab（Java、XML 一致）。
- 文件编码 **UTF-8**，无 BOM。
- 一个 Java 文件只放一个 public 类。
- import 不写通配符 `*`，单个类逐个导入。

## 版本控制

- 忽略规则统一写在项目根目录 `.gitignore`，前后端分节。
- 编译产物 `target/`、IDE 配置 `.idea/`、日志 `logs/` 不入库。
- 空目录 git 不跟踪，需放入实际文件后才会入库。

## 注释

### YAML

用 `#` **行内注释**，配置项同行末尾，两空格后跟 `#`：

```yaml
# 项目通用配置文件

server:
  port: 8081  # 端口

spring:
  application:
    name: coderank  # 项目名称
  profiles:
    active: dev  # 激活的配置文件
```

- 文件首行一行说明用途：`# 开发环境配置文件`。
- 注释用名词短语：`# 端口`、`# 项目名称`。
- 需要分组时用 `# 分组名` 单独一行。
- 不写大段横幅（`# =====`），不写配置优先级说明表。

### pom.xml

依赖注释写在 `<dependency>` **上一行**，`<!--` 与 `-->` 内部不加空格：

```xml
<!--web开发相关依赖-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

- 名词短语说明用途，8 空格缩进与 `<dependency>` 对齐。
- 需要解释「为什么选它」时用冒号：`<!--jjwt：生成解析 token 用-->`。

### Java

类用 Javadoc 说明"是什么"，完整陈述句带句号：

```java
/**
 * 统一响应体。
 */
public class Result<T> implements Serializable {
```

字段、常量用**单行 Javadoc**，写在字段上方：

```java
/** 状态码：0 表示成功，非 0 表示失败 */
private int code;

/** 成功状态码 */
public static final int CODE_SUCCESS = 0;
```

方法用 `//` 单行注释，写在方法上方，写成动词短语：

```java
// 健康检查
@GetMapping
public Result<Map<String, Object>> health() {

// 取第一条校验错误信息
private String firstErrorMessage(List<FieldError> fieldErrors) {
```

方法内部只在**需要解释为什么**时才注释：

```java
// 惰性初始化，不访问数据库就不会建连
```

不写 `@param`、`@return`、`@throws` 标签，参数含义靠命名表达。

### 标点

- 完整的陈述句以句号结尾：`/** 统一响应体。 */`
- 短标签式描述不加句号：`/** 成功状态码 */`、`# 端口`、`<!--web开发相关依赖-->`
- 字段、常量的短说明不加句号；类注释、成句说明加句号。
- 同一文件内保持统一。

## 总则

- 注释用**中文**书写。
- 注释解释**意图**，不复述代码。`// 返回用户名` 这类无信息量的注释不写。
- 该写的地方写，没补充价值的地方不写。
