# Database

MySQL 数据库脚本目录。

## 文件说明

- `schema.sql`：创建数据库与完整表结构。
- `seed.sql`：插入基础栏目、标签和少量塔科夫演示资料。

## 推荐执行顺序

1. 打开 Navicat Premium。
2. 连接本机 MySQL80。
3. 新建查询窗口。
4. 先执行 `schema.sql`。
5. 再执行 `seed.sql`。

## 数据库名称

```text
tarkov_community
```

## 演示账号

`seed.sql` 中的演示账号统一使用 BCrypt 密码，明文密码为：

```text
admin123
```

## 设计策略

本项目采用“社区主业务表 + 塔科夫资料表 + 帖子类型扩展表”的数据库结构。

社区功能优先保证完整可用；塔科夫资料表先保留结构和少量演示数据，后续可通过后台管理继续维护。

## 邮箱验证相关表

`email_verification_token` 用于保存注册邮箱确认 token 的哈希值、过期时间和验证状态。注册用户默认进入 `PENDING` 状态，完成邮箱确认后更新为 `NORMAL`，之后才允许登录。
