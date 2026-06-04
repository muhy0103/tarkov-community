# 逃离塔科夫玩家情报社区管理系统需求与构建计划

版本日期：2026-06-04

## 1. 项目定位

### 1.1 项目名称

推荐名称：逃离塔科夫玩家情报社区管理系统

可选名称：

- 逃离塔科夫玩家社区论坛管理系统
- 塔科夫玩家情报社区
- Tarkov 玩家战前情报社区

### 1.2 项目类型

本项目是一个基于 Vue3 + Spring Boot + MySQL 的前后端分离 Web 应用。

系统不做成传统 Wiki 百科站，而是做成以玩家交流为核心的现代社区平台。塔科夫特色主要体现在内容分类、帖子类型、资料关联、标签筛选和后台资料维护能力上。

### 1.3 项目一句话说明

本项目面向逃离塔科夫玩家，围绕地图情报、商人任务、装备弹药、市场经济、藏身处建设、Boss 情报、战局复盘和组队招募等玩家实战需求，构建一个集内容发布、评论互动、资料关联、举报审核和后台管理于一体的玩家情报社区系统。

### 1.4 项目建设策略

采用“重型架构，轻量数据录入”的策略。

具体含义：

- 社区功能必须完整可用。
- 数据库结构按较完整的塔科夫资料库方向设计。
- 地图、任务、商人、物品、弹药、藏身处、Boss 等资料表先建好。
- 初期只录入少量演示数据，不追求完整搬运 Wiki 内容。
- 后台提供资料维护能力，后续可以逐步扩充数据。

这样既能体现系统设计深度，也不会被大量游戏资料录入拖慢开发进度。

## 2. 总体技术架构

### 2.1 技术栈

前端：

- Vue3
- Vite
- Composition API
- script setup
- Vue Router 4
- Pinia
- Axios
- Element Plus

后端：

- Spring Boot
- Spring Web
- MyBatis-Plus
- MySQL
- JWT
- Spring Security 或自定义拦截器
- Validation 参数校验
- Lombok
- Swagger / Knife4j 接口文档

数据库：

- MySQL 8.x
- InnoDB
- utf8mb4
- 逻辑删除
- 统一时间字段

### 2.2 系统架构

```text
Vue3 前端 SPA
  |
  | Axios HTTP 请求
  v
Spring Boot REST API
  |
  | MyBatis-Plus
  v
MySQL 数据库
```

### 2.3 系统组成

系统分为三大部分：

1. 玩家社区前台
   - 浏览帖子
   - 搜索筛选
   - 发布内容
   - 评论互动
   - 点赞收藏
   - 举报内容
   - 个人中心

2. 塔科夫资料骨架
   - 地图资料
   - 商人资料
   - 任务资料
   - 物品资料
   - 武器弹药资料
   - 藏身处资料
   - Boss 与敌人资料

3. 后台管理系统
   - 用户管理
   - 帖子管理
   - 评论管理
   - 举报处理
   - 栏目与标签管理
   - 塔科夫资料维护
   - 社区统计
   - 操作日志

## 3. 用户角色与权限

### 3.1 游客

游客不需要登录，可以：

- 浏览首页
- 查看帖子列表
- 查看帖子详情
- 搜索帖子
- 按栏目、地图、标签筛选帖子
- 查看基础塔科夫资料

游客不能：

- 发帖
- 评论
- 点赞
- 收藏
- 举报
- 进入个人中心
- 进入后台管理

### 3.2 普通玩家

普通玩家登录后可以：

- 发布帖子
- 编辑自己的帖子
- 删除自己的帖子
- 发表评论
- 删除自己的评论
- 点赞和取消点赞
- 收藏和取消收藏
- 举报帖子或评论
- 查看我的帖子
- 查看我的收藏
- 修改个人资料

普通玩家不能：

- 删除他人的帖子
- 删除他人的评论
- 禁用用户
- 处理举报
- 维护系统基础资料

### 3.3 管理员

管理员可以：

- 登录后台
- 查看用户列表
- 禁用或恢复用户
- 查看所有帖子
- 下架违规帖子
- 删除违规评论
- 处理举报
- 管理栏目和标签
- 维护塔科夫资料数据
- 查看社区统计数据
- 查看操作日志

## 4. 前端需求

### 4.1 前端整体风格

视觉方向：

- 明亮
- 简洁
- 清爽
- 社区感
- 功能分区明确
- 避免传统 Wiki 页面冗杂感

不采用：

- 大面积黑色
- 大面积军绿色
- 生锈金属质感
- 高密度表格堆叠
- 复杂背景纹理
- 百科目录式首页

推荐主色：

```text
页面背景：#F6F8FA
卡片背景：#FFFFFF
主按钮：#0EA5A4
重点按钮：#2563EB
成功 / 撤离：#22C55E
警示 / 风险：#F59E0B
危险 / 举报：#EF4444
正文文字：#1F2937
次级文字：#6B7280
边框：#E5E7EB
```

### 4.2 前端页面结构

#### 4.2.1 前台页面

1. 登录页 `/login`
   - 用户名 / 邮箱
   - 密码
   - 登录按钮
   - 跳转注册
   - 表单校验

2. 注册页 `/register`
   - 用户名
   - 昵称
   - 密码
   - 确认密码
   - 注册按钮

3. 首页 `/`
   - 顶部导航栏
   - 搜索框
   - 发帖按钮
   - 推荐帖子
   - 热门讨论
   - 最新求助
   - 右侧战前简报

4. 帖子列表页 `/posts`
   - 分页列表
   - 关键词搜索
   - 栏目筛选
   - 地图筛选
   - 标签筛选
   - 风险等级筛选
   - 排序：最新、热门、评论最多、收藏最多

5. 帖子详情页 `/posts/:id`
   - 标题
   - 作者信息
   - 栏目与标签
   - 关联地图 / 商人 / 任务 / 物品
   - 正文
   - 点赞
   - 收藏
   - 评论列表
   - 发表评论
   - 举报入口

6. 发帖页 `/create-post`
   - 选择帖子类型
   - 动态表单
   - 标题
   - 正文
   - 栏目
   - 标签
   - 关联塔科夫资料
   - 发布按钮

7. 编辑帖子页 `/posts/:id/edit`
   - 回显帖子内容
   - 修改标题、正文、标签和扩展字段
   - 保存修改

8. 个人中心 `/profile`
   - 我的资料
   - 我的帖子
   - 我的收藏
   - 我的评论
   - 账号设置

9. 塔科夫资料浏览页 `/intel`
   - 地图
   - 商人
   - 任务
   - 物品
   - 弹药
   - 藏身处

#### 4.2.2 后台页面

后台路由建议统一放在 `/admin` 下，使用嵌套路由。

1. 后台首页 `/admin`
   - 用户数
   - 帖子数
   - 评论数
   - 举报数
   - 热门地图
   - 热门栏目

2. 用户管理 `/admin/users`
   - 用户列表
   - 搜索用户
   - 查看用户状态
   - 禁用用户
   - 恢复用户

3. 帖子管理 `/admin/posts`
   - 帖子列表
   - 按状态筛选
   - 查看详情
   - 下架帖子
   - 恢复帖子

4. 评论管理 `/admin/comments`
   - 评论列表
   - 按帖子或用户筛选
   - 删除违规评论

5. 举报处理 `/admin/reports`
   - 举报列表
   - 举报对象类型
   - 举报原因
   - 处理状态
   - 处理结果

6. 栏目管理 `/admin/categories`
   - 新增栏目
   - 编辑栏目
   - 启用 / 禁用栏目

7. 标签管理 `/admin/tags`
   - 新增标签
   - 编辑标签
   - 标签类型维护

8. 塔科夫资料维护 `/admin/tarkov`
   - 地图维护
   - 商人维护
   - 任务维护
   - 物品维护
   - 武器维护
   - 弹药维护
   - 藏身处维护
   - Boss 维护

9. 操作日志 `/admin/logs`
   - 管理员操作记录
   - 操作对象
   - 操作时间

### 4.3 前端核心组件

通用组件：

- `AppLayout`：整体页面布局
- `HeaderNav`：顶部导航
- `SideNav`：侧边导航
- `PostCard`：帖子卡片
- `TagBadge`：标签组件
- `RiskBadge`：风险等级标签
- `UserAvatar`：用户头像
- `EmptyState`：空状态
- `LoadingState`：加载状态
- `ConfirmDialog`：确认弹窗
- `BaseTable`：后台通用表格

业务组件：

- `PostList`
- `PostFilterBar`
- `PostDetail`
- `CommentList`
- `CommentItem`
- `CreatePostForm`
- `TaskPostFields`
- `LoadoutPostFields`
- `MarketPostFields`
- `RaidReviewFields`
- `TeamUpFields`
- `AdminStatCards`
- `ReportProcessDialog`

### 4.4 动态发帖表单

发帖时先选择帖子类型，再显示对应字段。

帖子类型：

1. 地图情报
2. 任务攻略
3. 配装方案
4. 市场讨论
5. 藏身处建设
6. Boss 情报
7. 战局复盘
8. 组队招募

任务攻略字段：

- 商人
- 任务
- 地图
- 任务类型
- 所需物品
- 推荐路线
- 风险等级
- 情报状态

配装方案字段：

- 武器
- 口径
- 推荐弹药
- 护甲等级
- 预算等级
- 适用地图
- 适合阶段

市场讨论字段：

- 物品
- 价格区间
- 用途类型
- 处理建议
- 是否任务需要
- 是否藏身处需要

战局复盘字段：

- 地图
- 死亡地点
- 敌人类型
- 装备损失
- 受伤状态
- 复盘结论

组队招募字段：

- 地图
- 目标类型
- 队伍人数
- 装备要求
- 语音要求
- 时间段
- 招募状态

### 4.5 Vue3 考核点对应设计

Vue3 核心基础：

- `ref`：搜索关键词、弹窗状态、加载状态、表单字段。
- `reactive`：登录表单、发帖表单、筛选条件。
- `computed`：筛选结果、当前用户权限、按钮状态、统计数据。
- `watch`：监听路由参数、搜索条件、栏目变化。
- `onMounted`：页面初始化加载数据。
- `v-for`：帖子列表、评论列表、标签列表。
- `v-if / v-show`：权限按钮、空状态、加载状态。
- `v-model`：所有表单输入。
- 动态样式：点赞状态、收藏状态、风险等级颜色。
- `Transition`：弹窗、列表切换、评论展开。

组件化：

- 帖子卡片组件复用。
- 后台表格组件复用。
- 父子组件通过 `props / emit` 通信。
- 可以使用 `provide / inject` 传递布局或用户上下文。
- 通用表格使用具名插槽。
- 个人中心标签页可用动态组件。
- 部分页面可使用 `KeepAlive` 缓存列表筛选状态。

Vue Router：

- 基础路由。
- 动态路由 `/posts/:id`。
- 后台嵌套路由。
- 登录路由守卫。
- 管理员权限守卫。
- 编程式导航。

Pinia：

- `userStore`
- `postStore`
- `categoryStore`
- `tarkovStore`
- `adminStore`

Axios：

- 统一请求实例。
- 请求拦截器添加 token。
- 响应拦截器处理错误。
- API 按模块拆分。

Element Plus：

- `ElForm`
- `ElInput`
- `ElSelect`
- `ElTable`
- `ElDialog`
- `ElTag`
- `ElPagination`
- `ElMenu`
- `ElTabs`

## 5. 后端需求

### 5.1 后端模块划分

```text
auth        登录注册、JWT认证
user        用户与个人资料
post        帖子发布、查询、编辑、删除
comment     评论与回复
interaction 点赞、收藏
category    栏目管理
tag         标签管理
tarkov      地图、商人、任务、物品、武器、弹药、藏身处、Boss资料
report      举报处理
admin       后台管理与统计
notice      公告与通知
file        头像、帖子图片上传
log         操作日志、登录日志
common      统一返回、异常处理、工具类
```

### 5.2 后端分层结构

```text
controller  接收前端请求
service     处理业务逻辑
mapper      数据库访问
entity      数据库实体
dto         请求参数对象
vo          响应视图对象
config      配置类
security    JWT与权限认证
exception   统一异常处理
common      统一返回结果
utils       工具类
```

### 5.3 认证与权限

登录流程：

1. 用户提交账号和密码。
2. 后端校验账号是否存在。
3. 校验密码是否正确。
4. 判断用户是否被禁用。
5. 生成 JWT。
6. 返回 token 和用户基本信息。
7. 前端保存 token。
8. 后续请求通过请求头携带 token。

权限控制：

- 未登录用户只能访问公开接口。
- 登录用户可以访问用户接口、发帖接口、评论接口。
- 管理员接口必须校验角色。
- 用户只能修改自己的帖子和评论。
- 管理员可以处理所有内容。

### 5.4 统一返回格式

建议所有接口统一返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

常用状态码：

- `200` 成功
- `400` 参数错误
- `401` 未登录
- `403` 无权限
- `404` 数据不存在
- `500` 系统异常

### 5.5 核心业务规则

用户规则：

- 用户名不可重复。
- 邮箱可选，但如果填写则不可重复。
- 密码必须加密存储。
- 被禁用用户不能登录。
- 管理员不能随意被普通接口修改角色。

帖子规则：

- 登录用户才能发帖。
- 标题不能为空。
- 正文不能为空。
- 帖子必须属于一个栏目。
- 帖子可以绑定多个标签。
- 帖子可以可选关联地图、任务、商人、物品、武器、弹药。
- 用户只能编辑自己的帖子。
- 删除帖子采用逻辑删除。
- 管理员可以下架帖子。

评论规则：

- 登录用户才能评论。
- 评论必须属于某个帖子。
- 支持一级评论和回复评论。
- 评论删除采用状态标记。
- 删除评论后帖子评论数需要更新。

点赞收藏规则：

- 一个用户对同一帖子只能点赞一次。
- 一个用户对同一帖子只能收藏一次。
- 再次点击取消点赞或收藏。
- 点赞、收藏数量需要同步更新到帖子表。

举报规则：

- 登录用户才能举报。
- 可以举报帖子或评论。
- 同一用户不应重复举报同一对象。
- 管理员处理后记录处理结果。
- 处理举报时可选择忽略、下架内容、删除评论、禁用用户。

后台规则：

- 所有后台接口需要管理员权限。
- 后台关键操作写入操作日志。
- 统计数据从用户、帖子、评论、举报等表计算。

## 6. 数据库需求

### 6.1 MySQL 设计规范

统一规范：

- 表名使用小写下划线。
- 主键字段统一为 `id BIGINT`。
- 使用 `created_at` 和 `updated_at`。
- 使用 `deleted` 表示逻辑删除。
- 状态字段使用 `status`。
- 字符集使用 `utf8mb4`。
- 存储引擎使用 `InnoDB`。
- 需要防重复的数据添加唯一索引。

### 6.2 用户与权限表

`sys_user` 用户表：

| 字段 | 含义 |
| --- | --- |
| id | 用户ID |
| username | 用户名 |
| password | 加密密码 |
| nickname | 昵称 |
| email | 邮箱 |
| avatar | 头像地址 |
| role | 角色：USER / ADMIN |
| status | 状态：NORMAL / DISABLED |
| contribution | 贡献值 |
| last_login_at | 最后登录时间 |
| created_at | 创建时间 |
| updated_at | 更新时间 |
| deleted | 逻辑删除 |

`user_profile` 用户资料表：

| 字段 | 含义 |
| --- | --- |
| id | 主键 |
| user_id | 用户ID |
| bio | 个人简介 |
| favorite_maps | 常用地图 |
| play_style | 游戏风格 |
| server_region | 常用服务器 |
| created_at | 创建时间 |
| updated_at | 更新时间 |

`login_log` 登录日志表：

| 字段 | 含义 |
| --- | --- |
| id | 主键 |
| user_id | 用户ID |
| username | 登录用户名 |
| ip | IP地址 |
| success | 是否成功 |
| message | 登录结果 |
| created_at | 登录时间 |

### 6.3 社区内容表

`category` 栏目表：

| 字段 | 含义 |
| --- | --- |
| id | 栏目ID |
| name | 栏目名称 |
| code | 栏目标识 |
| description | 栏目说明 |
| icon | 图标 |
| sort_order | 排序 |
| status | 状态 |
| created_at | 创建时间 |
| updated_at | 更新时间 |

默认栏目：

- 战区地图
- 任务档案
- 装备弹药
- 市场经济
- 藏身处
- 敌人与 Boss
- 战局复盘
- 组队招募

`tag` 标签表：

| 字段 | 含义 |
| --- | --- |
| id | 标签ID |
| name | 标签名称 |
| type | 标签类型：MAP / RISK / PLAY_STYLE / ITEM / SYSTEM |
| color | 标签颜色 |
| status | 状态 |
| created_at | 创建时间 |
| updated_at | 更新时间 |

`post` 帖子主表：

| 字段 | 含义 |
| --- | --- |
| id | 帖子ID |
| user_id | 作者ID |
| category_id | 栏目ID |
| title | 标题 |
| content | 正文 |
| post_type | 帖子类型 |
| cover_image | 封面图 |
| map_id | 关联地图ID，可空 |
| trader_id | 关联商人ID，可空 |
| quest_id | 关联任务ID，可空 |
| item_id | 关联物品ID，可空 |
| weapon_id | 关联武器ID，可空 |
| ammo_id | 关联弹药ID，可空 |
| risk_level | 风险等级 |
| intel_status | 情报状态 |
| view_count | 浏览数 |
| like_count | 点赞数 |
| favorite_count | 收藏数 |
| comment_count | 评论数 |
| status | 状态：NORMAL / PENDING / OFFLINE |
| pinned | 是否置顶 |
| recommended | 是否推荐 |
| created_at | 创建时间 |
| updated_at | 更新时间 |
| deleted | 逻辑删除 |

`post_tag` 帖子标签关联表：

| 字段 | 含义 |
| --- | --- |
| id | 主键 |
| post_id | 帖子ID |
| tag_id | 标签ID |
| created_at | 创建时间 |

### 6.4 帖子类型扩展表

`post_task_detail` 任务攻略详情：

| 字段 | 含义 |
| --- | --- |
| id | 主键 |
| post_id | 帖子ID |
| quest_id | 任务ID |
| trader_id | 商人ID |
| map_id | 地图ID |
| task_type | 任务类型 |
| required_items | 所需物品 |
| route_advice | 推荐路线 |
| risk_level | 风险等级 |
| intel_status | 情报状态 |

`post_loadout_detail` 配装方案详情：

| 字段 | 含义 |
| --- | --- |
| id | 主键 |
| post_id | 帖子ID |
| weapon_id | 武器ID |
| ammo_id | 弹药ID |
| armor_level | 护甲等级 |
| budget_level | 预算等级 |
| suitable_maps | 适用地图 |
| suitable_stage | 适合阶段 |
| loadout_summary | 配装说明 |

`post_market_detail` 市场讨论详情：

| 字段 | 含义 |
| --- | --- |
| id | 主键 |
| post_id | 帖子ID |
| item_id | 物品ID |
| price_range | 价格区间 |
| usage_type | 用途类型 |
| suggestion | 处理建议 |
| quest_needed | 是否任务需要 |
| hideout_needed | 是否藏身处需要 |

`post_raid_review_detail` 战局复盘详情：

| 字段 | 含义 |
| --- | --- |
| id | 主键 |
| post_id | 帖子ID |
| map_id | 地图ID |
| death_location | 死亡地点 |
| enemy_type | 敌人类型 |
| lost_equipment | 装备损失 |
| injury_status | 受伤状态 |
| review_summary | 复盘总结 |

`post_teamup_detail` 组队招募详情：

| 字段 | 含义 |
| --- | --- |
| id | 主键 |
| post_id | 帖子ID |
| map_id | 地图ID |
| goal_type | 目标类型 |
| team_size | 队伍人数 |
| gear_requirement | 装备要求 |
| voice_requirement | 语音要求 |
| play_time | 时间段 |
| recruit_status | 招募状态 |

### 6.5 评论与互动表

`comment` 评论表：

| 字段 | 含义 |
| --- | --- |
| id | 评论ID |
| post_id | 帖子ID |
| user_id | 评论用户ID |
| parent_id | 父评论ID |
| reply_to_user_id | 回复对象用户ID |
| content | 评论内容 |
| like_count | 点赞数 |
| status | 状态 |
| created_at | 创建时间 |
| updated_at | 更新时间 |
| deleted | 逻辑删除 |

`post_like` 帖子点赞表：

| 字段 | 含义 |
| --- | --- |
| id | 主键 |
| post_id | 帖子ID |
| user_id | 用户ID |
| created_at | 创建时间 |

唯一约束：`post_id + user_id`

`comment_like` 评论点赞表：

| 字段 | 含义 |
| --- | --- |
| id | 主键 |
| comment_id | 评论ID |
| user_id | 用户ID |
| created_at | 创建时间 |

唯一约束：`comment_id + user_id`

`favorite` 收藏表：

| 字段 | 含义 |
| --- | --- |
| id | 主键 |
| post_id | 帖子ID |
| user_id | 用户ID |
| created_at | 创建时间 |

唯一约束：`post_id + user_id`

### 6.6 举报、通知与日志表

`report` 举报表：

| 字段 | 含义 |
| --- | --- |
| id | 举报ID |
| reporter_id | 举报人ID |
| target_type | 举报对象类型：POST / COMMENT |
| target_id | 举报对象ID |
| reason | 举报原因 |
| description | 补充说明 |
| status | 状态：PENDING / PROCESSED / REJECTED |
| handler_id | 处理人ID |
| handle_result | 处理结果 |
| handled_at | 处理时间 |
| created_at | 创建时间 |

`notification` 通知表：

| 字段 | 含义 |
| --- | --- |
| id | 通知ID |
| user_id | 接收用户ID |
| type | 通知类型 |
| title | 通知标题 |
| content | 通知内容 |
| related_id | 关联对象ID |
| read_status | 是否已读 |
| created_at | 创建时间 |

`announcement` 公告表：

| 字段 | 含义 |
| --- | --- |
| id | 公告ID |
| title | 标题 |
| content | 内容 |
| status | 状态 |
| created_by | 创建人 |
| created_at | 创建时间 |
| updated_at | 更新时间 |

`operation_log` 操作日志表：

| 字段 | 含义 |
| --- | --- |
| id | 日志ID |
| admin_id | 管理员ID |
| action | 操作类型 |
| target_type | 操作对象类型 |
| target_id | 操作对象ID |
| detail | 操作详情 |
| ip | IP地址 |
| created_at | 创建时间 |

### 6.7 塔科夫资料表

`tarkov_map` 地图表：

| 字段 | 含义 |
| --- | --- |
| id | 地图ID |
| name_en | 英文名 |
| name_zh | 中文名 |
| difficulty | 难度 |
| description | 简介 |
| recommended_level | 推荐等级 |
| status | 状态 |
| created_at | 创建时间 |
| updated_at | 更新时间 |

`map_extract` 撤离点表：

| 字段 | 含义 |
| --- | --- |
| id | 撤离点ID |
| map_id | 地图ID |
| name | 撤离点名称 |
| faction_limit | 阵营限制 |
| condition_text | 撤离条件 |
| description | 说明 |
| status | 状态 |

`map_loot_area` 物资区域表：

| 字段 | 含义 |
| --- | --- |
| id | 区域ID |
| map_id | 地图ID |
| name | 区域名称 |
| loot_type | 物资类型 |
| risk_level | 风险等级 |
| description | 说明 |

`tarkov_trader` 商人表：

| 字段 | 含义 |
| --- | --- |
| id | 商人ID |
| name_en | 英文名 |
| name_zh | 中文名 |
| description | 简介 |
| unlock_condition | 解锁条件 |
| avatar | 头像 |
| status | 状态 |

`tarkov_quest` 任务表：

| 字段 | 含义 |
| --- | --- |
| id | 任务ID |
| trader_id | 所属商人 |
| name_en | 英文名 |
| name_zh | 中文名 |
| quest_type | 任务类型 |
| map_id | 关联地图 |
| description | 任务说明 |
| rewards | 奖励简介 |
| unlocks | 解锁内容 |
| status | 状态 |

`quest_prerequisite` 任务前置关系表：

| 字段 | 含义 |
| --- | --- |
| id | 主键 |
| quest_id | 当前任务ID |
| prerequisite_quest_id | 前置任务ID |

`tarkov_item` 物品表：

| 字段 | 含义 |
| --- | --- |
| id | 物品ID |
| name_en | 英文名 |
| name_zh | 中文名 |
| item_type | 物品类型 |
| rarity | 稀有度 |
| grid_size | 占格大小 |
| quest_needed | 是否任务需要 |
| hideout_needed | 是否藏身处需要 |
| keep_suggestion | 是否建议保留 |
| description | 说明 |
| status | 状态 |

`tarkov_weapon` 武器表：

| 字段 | 含义 |
| --- | --- |
| id | 武器ID |
| name_en | 英文名 |
| name_zh | 中文名 |
| weapon_type | 武器类型 |
| caliber | 口径 |
| description | 说明 |
| status | 状态 |

`tarkov_ammo` 弹药表：

| 字段 | 含义 |
| --- | --- |
| id | 弹药ID |
| name_en | 英文名 |
| name_zh | 中文名 |
| caliber | 口径 |
| damage | 伤害 |
| penetration | 穿透 |
| armor_damage | 护甲伤害 |
| description | 说明 |
| status | 状态 |

`hideout_station` 藏身处模块表：

| 字段 | 含义 |
| --- | --- |
| id | 模块ID |
| name_en | 英文名 |
| name_zh | 中文名 |
| description | 说明 |
| status | 状态 |

`hideout_upgrade` 藏身处升级表：

| 字段 | 含义 |
| --- | --- |
| id | 升级ID |
| station_id | 模块ID |
| level | 等级 |
| required_items | 所需材料 |
| required_time | 所需时间 |
| unlocks | 解锁功能 |

`boss` Boss 表：

| 字段 | 含义 |
| --- | --- |
| id | Boss ID |
| name_en | 英文名 |
| name_zh | 中文名 |
| map_id | 常见地图 |
| description | 特点 |
| equipment_summary | 装备简介 |
| status | 状态 |

## 7. 接口需求

### 7.1 认证接口

```text
POST /api/auth/register     用户注册
POST /api/auth/login        用户登录
GET  /api/auth/me           获取当前用户
POST /api/auth/logout       退出登录
```

### 7.2 用户接口

```text
GET  /api/users/me                  获取个人资料
PUT  /api/users/me                  修改个人资料
GET  /api/users/me/posts            我的帖子
GET  /api/users/me/favorites        我的收藏
GET  /api/users/me/comments         我的评论
GET  /api/users/me/notifications    我的通知
```

### 7.3 帖子接口

```text
GET    /api/posts              分页查询帖子
GET    /api/posts/{id}         查看帖子详情
POST   /api/posts              发布帖子
PUT    /api/posts/{id}         编辑帖子
DELETE /api/posts/{id}         删除帖子
GET    /api/posts/hot          热门帖子
GET    /api/posts/recommended  推荐帖子
```

帖子查询参数：

```text
keyword
categoryId
tagId
mapId
traderId
questId
postType
riskLevel
intelStatus
sort
page
pageSize
```

### 7.4 评论接口

```text
GET    /api/posts/{postId}/comments       查询评论
POST   /api/posts/{postId}/comments       发表评论
DELETE /api/comments/{id}                 删除评论
POST   /api/comments/{id}/like            点赞评论
DELETE /api/comments/{id}/like            取消点赞评论
```

### 7.5 互动接口

```text
POST   /api/posts/{id}/like       点赞帖子
DELETE /api/posts/{id}/like       取消点赞
POST   /api/posts/{id}/favorite   收藏帖子
DELETE /api/posts/{id}/favorite   取消收藏
```

### 7.6 栏目与标签接口

```text
GET /api/categories   查询栏目
GET /api/tags         查询标签
```

### 7.7 塔科夫资料接口

```text
GET /api/tarkov/maps
GET /api/tarkov/traders
GET /api/tarkov/quests
GET /api/tarkov/items
GET /api/tarkov/weapons
GET /api/tarkov/ammo
GET /api/tarkov/hideout/stations
GET /api/tarkov/bosses
```

### 7.8 举报接口

```text
POST /api/reports       提交举报
GET  /api/reports/me    我的举报
```

### 7.9 后台接口

```text
GET    /api/admin/statistics       后台统计
GET    /api/admin/users            用户列表
PUT    /api/admin/users/{id}/status 修改用户状态
GET    /api/admin/posts            帖子管理列表
PUT    /api/admin/posts/{id}/status 修改帖子状态
GET    /api/admin/comments         评论管理列表
PUT    /api/admin/comments/{id}/status 修改评论状态
GET    /api/admin/reports          举报列表
PUT    /api/admin/reports/{id}/handle 处理举报
GET    /api/admin/logs             操作日志
```

后台资料维护接口可统一采用：

```text
GET    /api/admin/tarkov/{type}
POST   /api/admin/tarkov/{type}
PUT    /api/admin/tarkov/{type}/{id}
DELETE /api/admin/tarkov/{type}/{id}
```

其中 `type` 可为：

- maps
- traders
- quests
- items
- weapons
- ammo
- hideout-stations
- bosses

## 8. 功能优先级

### 8.1 P0 必须完成

- 前后端项目搭建
- MySQL 数据库连接
- 用户注册登录
- JWT 认证
- 普通用户和管理员角色
- 帖子发布
- 帖子列表
- 帖子详情
- 帖子编辑和删除
- 评论发布和查询
- 点赞和收藏
- 栏目和标签筛选
- 后台用户管理
- 后台帖子管理
- 后台评论管理
- Vue Router
- Pinia
- Axios
- Element Plus

### 8.2 P1 重点加分

- 举报处理
- 操作日志
- 后台统计看板
- 塔科夫资料维护
- 动态发帖表单
- 帖子类型扩展表
- 路由守卫
- Axios 拦截器
- 插槽组件
- KeepAlive
- Transition 动画

### 8.3 P2 有时间再做

- 通知系统
- 登录日志
- 头像上传
- 帖子图片上传
- 藏身处升级详情
- 地图撤离点
- 地图物资点
- Boss 刷新区域
- 更复杂的推荐排序

## 9. 四周构建计划

### 第 1 周：需求定稿、数据库设计、基础框架

目标：

完成项目基础设施，让前后端和数据库能跑起来。

任务：

1. 完成项目需求文档。
2. 完成数据库 ER 设计。
3. 创建 MySQL 数据库。
4. 创建 Spring Boot 项目。
5. 配置 MyBatis-Plus。
6. 配置统一返回结果。
7. 配置统一异常处理。
8. 配置跨域。
9. 搭建 Vue3 + Vite 项目。
10. 配置 Vue Router。
11. 配置 Pinia。
12. 配置 Axios。
13. 配置 Element Plus。
14. 完成登录注册接口。
15. 完成登录注册页面。

验收标准：

- 前端能启动。
- 后端能启动。
- 后端能连接 MySQL。
- 登录注册接口可用。
- 前端能调用后端接口。
- 数据库核心表已经创建。

### 第 2 周：社区核心功能

目标：

完成一个可用的玩家社区闭环。

任务：

1. 完成 JWT 登录认证。
2. 完成用户信息接口。
3. 完成帖子发布接口。
4. 完成帖子列表接口。
5. 完成帖子详情接口。
6. 完成帖子编辑和删除接口。
7. 完成分页查询。
8. 完成关键词搜索。
9. 完成栏目筛选。
10. 完成标签筛选。
11. 完成评论发布接口。
12. 完成评论列表接口。
13. 完成评论删除接口。
14. 完成点赞和取消点赞。
15. 完成收藏和取消收藏。
16. 完成前端首页。
17. 完成帖子列表页。
18. 完成帖子详情页。
19. 完成发帖页。
20. 完成个人中心基础页面。

验收标准：

- 用户可以注册、登录、发帖、看帖、评论、点赞、收藏。
- 帖子数据来自真实后端接口。
- 个人中心能看到我的帖子和我的收藏。
- 前端路由、Pinia、Axios 已经实际使用。

### 第 3 周：塔科夫特色、后台管理、举报审核

目标：

让项目从普通论坛升级为塔科夫玩家情报社区。

任务：

1. 完成地图资料表接口。
2. 完成商人资料表接口。
3. 完成任务资料表接口。
4. 完成物品资料表接口。
5. 完成武器资料表接口。
6. 完成弹药资料表接口。
7. 完成帖子关联塔科夫资料。
8. 完成动态发帖表单。
9. 完成任务攻略扩展字段。
10. 完成配装方案扩展字段。
11. 完成市场讨论扩展字段。
12. 完成战局复盘扩展字段。
13. 完成组队招募扩展字段。
14. 完成管理员路由守卫。
15. 完成后台首页。
16. 完成用户管理。
17. 完成帖子管理。
18. 完成评论管理。
19. 完成举报提交。
20. 完成举报处理。
21. 完成后台统计接口。
22. 完成操作日志。

验收标准：

- 管理员可以进入后台。
- 管理员可以管理用户、帖子、评论和举报。
- 前台可以发布不同类型的塔科夫特色帖子。
- 帖子可以关联地图、商人、任务、物品、武器或弹药。
- 后台可以维护基础塔科夫资料。

### 第 4 周：联调、优化、测试、报告与答辩

目标：

完成项目收尾，保证可演示、可提交、可答辩。

任务：

1. 完成前后端全流程联调。
2. 修复接口错误。
3. 修复页面显示问题。
4. 补充少量演示数据。
5. 优化首页布局。
6. 优化帖子列表筛选体验。
7. 优化发帖表单交互。
8. 优化后台管理表格。
9. 添加加载状态。
10. 添加空状态。
11. 添加错误提示。
12. 添加 Transition 动画。
13. 添加 KeepAlive。
14. 整理接口文档。
15. 整理项目目录说明。
16. 截取运行截图。
17. 撰写项目报告。
18. 准备答辩讲解稿。
19. 准备 2 到 3 个常见技术问题回答。
20. 打包源码和报告。

验收标准：

- 项目可以完整演示。
- 前端页面风格统一、清爽简洁。
- 后端接口稳定。
- 数据库结构清晰。
- 报告内容完整。
- 答辩能讲清楚 Vue3、Spring Boot 和 MySQL 的实现重点。

## 10. 演示数据策略

不需要录入完整塔科夫 Wiki 数据，只需要准备少量样例。

建议演示数据：

地图：

- Customs
- Factory
- Woods
- Reserve
- Streets of Tarkov

商人：

- Prapor
- Therapist
- Skier
- Mechanic
- Ragman

任务：

- Debut
- Delivery From the Past
- Shortage
- Gunsmith Part 1

物品：

- Salewa
- Secure Flash Drive
- Gas Analyzer
- Graphics Card

武器：

- AK-74N
- M4A1
- MP-153

弹药：

- 5.45x39mm BP
- 5.56x45mm M855A1
- 7.62x39mm PS

帖子：

- 任务攻略帖 3 条
- 配装方案帖 3 条
- 战局复盘帖 2 条
- 市场讨论帖 2 条
- 组队招募帖 2 条

用户：

- 管理员 1 个
- 普通用户 3 个

## 11. 项目报告结构

建议报告按以下结构写：

1. 项目概述
   - 项目背景
   - 项目目标
   - 项目特色

2. 技术选型
   - Vue3
   - Spring Boot
   - MySQL
   - MyBatis-Plus
   - Pinia
   - Axios
   - Element Plus

3. 系统需求分析
   - 用户角色
   - 功能需求
   - 非功能需求

4. 系统架构设计
   - 前后端分离架构
   - 前端目录结构
   - 后端分层结构
   - 数据流说明

5. 数据库设计
   - 表结构设计
   - 表关系说明
   - 核心字段说明

6. 前端实现
   - 路由设计
   - 组件设计
   - Pinia 状态管理
   - Axios 请求封装
   - 动态发帖表单

7. 后端实现
   - 登录认证
   - 权限控制
   - 帖子模块
   - 评论模块
   - 举报审核
   - 后台管理

8. 系统测试
   - 登录测试
   - 发帖测试
   - 评论测试
   - 点赞收藏测试
   - 后台管理测试

9. 运行截图
   - 登录页
   - 首页
   - 帖子列表
   - 帖子详情
   - 发帖页
   - 个人中心
   - 后台首页
   - 用户管理
   - 举报处理

10. 总结与反思
   - 遇到的问题
   - 解决方案
   - 项目收获
   - 后续改进方向

## 12. 答辩重点

前端可以讲：

- 为什么使用 Vue3 + Vite。
- Composition API 如何组织页面逻辑。
- Pinia 如何管理用户和帖子状态。
- Vue Router 如何实现动态路由和路由守卫。
- Axios 如何封装请求和拦截器。
- 动态发帖表单如何体现组件化和条件渲染。

后端可以讲：

- 为什么采用前后端分离。
- Spring Boot 后端如何分层。
- JWT 登录认证流程。
- 普通用户和管理员权限如何区分。
- 帖子、评论、点赞、收藏之间的数据关系。
- 举报处理如何体现社区管理。
- MyBatis-Plus 如何实现分页查询。

数据库可以讲：

- 为什么采用帖子主表 + 类型扩展表。
- 为什么塔科夫资料单独建表。
- 点赞和收藏为什么需要唯一约束。
- 为什么采用逻辑删除。
- 如何支持后续扩展更多塔科夫资料。

## 13. 风险与应对

风险 1：功能范围过大。

应对：

- P0 功能优先。
- P1 功能作为加分。
- P2 功能有时间再做。

风险 2：塔科夫资料录入耗时。

应对：

- 只录入演示数据。
- 后台提供维护能力。
- 把完整资料库作为扩展方向。

风险 3：前后端联调问题。

应对：

- 第 1 周就打通登录接口。
- 第 2 周所有核心功能都用真实接口。
- 不把联调压到最后一周。

风险 4：权限控制复杂。

应对：

- 先实现 USER / ADMIN 两种角色。
- 管理员接口统一加权限校验。
- 用户只能操作自己的内容。

风险 5：报告时间不足。

应对：

- 开发过程中同步保存截图。
- 每完成一个模块记录实现思路。
- 第 4 周集中整理报告。

## 14. 最终交付物

最终需要提交：

1. 前端源码
2. 后端源码
3. MySQL 建表 SQL
4. 少量演示数据 SQL
5. 项目报告 Word 文档
6. 运行截图
7. 接口文档
8. 项目打包压缩文件

推荐压缩包命名：

```text
学号姓名-逃离塔科夫玩家情报社区管理系统.zip
```

报告命名：

```text
学号姓名-逃离塔科夫玩家情报社区管理系统报告.docx
```

## 15. 最终范围建议

最推荐的最终实现范围：

- 前台社区完整可用。
- 后台管理完整可演示。
- 塔科夫资料表结构完整。
- 塔科夫资料录入少量演示数据。
- 帖子能关联地图、任务、商人、物品、武器、弹药。
- 动态发帖表单作为前端亮点。
- 帖子主表 + 类型扩展表作为数据库亮点。
- JWT + 权限控制作为后端亮点。
- 清爽明亮社区界面作为视觉亮点。

这个范围比较适合四周完成，也能同时满足 Vue3 和 JavaEE 两门课程的展示需要。

