# CodeRank 前端开发规范

CodeRank 由平级的 `backend/` 和 `frontend/` 组成；本文件约束 `frontend/`，后端规则见 `../backend/AGENTS.md`。

## 技术栈

| 组件 | 版本 |
|---|---|
| Vue | 3.5+ |
| Vite | 8.x |
| TypeScript | 6.0 |
| Vue Router | 5.x |
| Pinia | 4.x |
| 包管理器 | npm（提交 `package-lock.json`） |

## 目录结构

```text
frontend/
├─ public/                     不经 Vite 打包的静态文件
├─ src/
│  ├─ api/                     接口请求，按后端分组分目录
│  │  └─ <分组>/<业务>.ts
│  ├─ assets/
│  │  ├─ css/                  全局基础样式
│  │  ├─ fonts/                本地字体文件
│  │  └─ images/               图片素材
│  ├─ components/              跨页面组件，按用途分目录
│  ├─ router/
│  │  ├─ index.ts              创建路由实例
│  │  ├─ routes.ts             汇总模块路由
│  │  └─ modules/<模块>.ts     各模块路由
│  ├─ stores/<模块>.ts         跨模块状态
│  ├─ utils/
│  │  ├─ requestUtil.ts        请求封装与错误处理
│  │  └─ <用途>Util.ts         其他工具
│  ├─ views/<模块>/
│  │  ├─ LayoutView.vue        模块布局
│  │  ├─ pages/                页面组件
│  │  ├─ components/           模块专用组件
│  │  └─ utils/                模块专用工具
│  ├─ App.vue                  路由出口
│  └─ main.ts                  应用初始化
├─ .env.example                环境变量示例
├─ vite.config.ts              Vite 配置和开发代理
└─ package.json                依赖与脚本
```

- 新页面按模块放在 `src/views/<模块>/`，模块内页面、组件、工具分别放 `pages/`、`components/`、`utils/`；需要跨模块共享时再下沉到 `src/components/` 或 `src/utils/`。
- 公共层不依赖 `src/views/`，页面模块之间不直接互相引用。
- 跨目录引用用 `@` 指向 `src/`，模块内部用相对路径。
- 暂无实现的目录用 `.gitkeep` 保留，文件内只写一行 `#` 开头的中文用途短语。

## 路由

- 每个模块的路由放 `src/router/modules/<模块>.ts`；`routes.ts` 只汇总，`index.ts` 只创建实例。
- 页面组件命名为 `XxxView.vue`，普通组件用 PascalCase。
- 需要登录的页面在 `meta` 里声明 `requiresAuth: true` 和 `roles`。
- 路由跳转用 `name` 而非硬编码 `path`。

## 接口

- 接口请求统一走 `src/utils/requestUtil.ts`，不直接用 axios。
- 按后端分组放 `src/api/<分组>/`，组内文件按业务命名。
- 后端统一返回 `{ code, msg, data }`，`code` 为 `0` 表示成功。
- 开发环境通过 Vite 代理转发，`/api` 前缀由代理剥离。
- `.env.example` 记录环境变量格式，不提交真实密钥。

## 状态管理

- 跨模块共享状态放 `src/stores/`，用 Pinia 组合式写法（`defineStore` + `setup`）。
- 模块内临时状态留在页面组件里，不放进 store。

## 代码风格

- Vue 单文件组件统一用 `<script setup lang="ts">`。
- 新建页面组件保留完整的 `<template>`、`<script setup lang="ts">`、`<style scoped>` 三段结构，即使暂时为空也保留空标签。
- 页面内容未确定时只搭空结构，**不填充占位文案或模拟数据**。
- 格式化遵循 `.prettierrc.json`：无分号、单引号、行宽 100。
- 工具文件命名 `xxxUtil.ts`。

## 注释

### Vue 组件

组件用途用**一行** `<!-- -->` 注释放在 `<template>` 前，说明"这是什么"：

```vue
<!-- 应用根组件 -->
<template>
```

### TypeScript 文件

文件首行用 `//` 说明文件职责：

```ts
// 首页公开分类接口
```

### 类型与函数

类型定义、函数上方用 `//` 单行注释，写成名词短语或动词短语：

```ts
// 首页侧栏展示的分类
export interface HomeCategory {
  id: number
  name: string
}

// 获取全部分类，按关联文章数量倒序
export async function listHomeCategories(): Promise<HomeCategory[]> {
```

### 方法内部

只在**需要解释为什么**时才注释，说明动机、取舍或踩坑点：

```ts
// 动态引入用户 Store，避免与它对本模块的依赖形成循环引用。
```

### 标点

- 短标签式说明**不加句号**：`// 认证接口请求`、`<!-- 应用根组件 -->`
- 成句的完整陈述**加句号**：`// 避免与它对本模块的依赖形成循环引用。`
- 同一文件内保持统一。

### .gitkeep

空目录占位文件只写一行 `#` 开头的中文用途短语，不写成句子：

```
# 相册专用组件
```

## 验证

- 新增页面或修改配置后至少运行 `npm run type-check` 和 `npm run build`，确认路由可正常访问。
- Windows 下运行 `npm.cmd run dev`、`npm.cmd run type-check`、`npm.cmd run build`。
