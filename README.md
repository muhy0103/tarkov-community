# 逃离塔科夫玩家情报社区管理系统

这是一个面向课程期末考核的前后端分离项目，主题为“逃离塔科夫玩家社区论坛管理系统”。项目采用 Vue 3 前端、Spring Boot 后端和 MySQL 数据库，目标不是做传统 wiki，而是做一个清爽、功能分明、偏玩家社区建设的情报论坛。

## 当前进度

- 已完成项目需求与构建计划文档：`tarkov-community-requirements-plan.md`
- 已完成 Vue 3 前端基础工程，使用 JavaScript、Vue Router、Pinia、Axios、Element Plus
- 已完成 Spring Boot 后端基础工程，接入 MySQL、MyBatis-Plus、统一响应和统一异常处理
- 已完成 32 张业务表的 MySQL 建表脚本和少量演示种子数据
- 已完成塔科夫资料接口：分区、标签、地图、商人、任务、物品、武器、弹药、藏身处、Boss
- 已完成论坛核心接口：帖子列表、帖子详情、发帖、评论、点赞、收藏
- 已完成注册登录接口和前端登录态保存
- 已完成后台概览统计接口，可聚合用户、帖子、评论和塔科夫资料数量
- 已完成后台帖子审核接口，可按状态、分区、关键词分页查询帖子，并更新帖子状态、推荐和置顶
- 已完成后台用户管理接口，可按角色、状态、关键词分页查询用户，并更新用户角色和状态
- 已完成前端首页、情报广场、登录注册页、帖子详情互动页、发布情报帖页面、后台概览页、后台帖子审核页

更详细的阶段记录见：`docs/development-progress.md`

## 本地运行

后端服务：

```powershell
cd C:\Users\muhy\Desktop\Study\tarkov-community\backend
.\mvnw spring-boot:run
```

前端服务：

```powershell
cd C:\Users\muhy\Desktop\Study\tarkov-community\frontend
npm run dev
```

常用访问地址：

- 前端页面：http://127.0.0.1:5173/
- 后端健康检查：http://127.0.0.1:8080/api/health

## 技术栈

- 前端：Vue 3、Vite、JavaScript、Vue Router、Pinia、Axios、Element Plus
- 后端：Spring Boot、MyBatis-Plus、Spring Security Crypto、MySQL
- 数据库：MySQL，建表脚本位于 `database/schema.sql`
- 版本管理：Git + GitHub

## 开发节奏

项目按“小模块开发、小模块验证、小模块提交推送”的方式推进。当前优先级是先把社区核心闭环跑通，再逐步补后台管理、权限控制、资料维护和课程报告素材。
