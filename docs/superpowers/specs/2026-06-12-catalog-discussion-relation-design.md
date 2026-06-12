# 用户端增强第四阶段：资料中心与社区讨论联动设计

## 背景

项目已经具备塔科夫资料展示、资料详情页、资料图片维护和基础论坛能力。当前缺口是资料与玩家讨论之间还没有稳定关系：用户进入地图、Boss、武器等资料后，无法看到围绕该资料展开的讨论；用户发帖时也无法明确标记帖子关联的塔科夫资料。

本阶段采用用户确认的重型方案：新增独立多对多关联表，让一个帖子可以关联多个资料对象，也让一个资料对象可以聚合多个帖子。

## 目标

1. 建立帖子与塔科夫资料之间的多对多关系。
2. 新增资料中心页面，作为资料浏览的主入口。
3. 发帖和编辑帖子时允许选择关联资料。
4. 帖子列表、帖子详情展示关联资料标签。
5. 资料详情页展示相关讨论，形成“资料 -> 讨论 -> 资料”的社区闭环。

## 非目标

1. 本阶段不实现图片上传，仍使用图片链接维护。
2. 本阶段不做复杂全文搜索引擎，仅使用现有数据库查询和前端筛选体验。
3. 本阶段不做关联资料的后台审核流程，帖子审核仍沿用现有帖子管理逻辑。
4. 本阶段不迁移旧帖子已有的 `map_id`、`weapon_id` 等字段数据，后续如有真实数据再做迁移脚本。
5. 本阶段不删除 `post` 表中已经存在的旧外键字段，避免影响已有结构和后续课程展示。

## 数据库设计

新增表：`post_catalog_relation`

字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT | 主键 |
| `post_id` | BIGINT NOT NULL | 关联帖子 |
| `catalog_type` | VARCHAR(30) NOT NULL | 资料类型 |
| `catalog_id` | BIGINT NOT NULL | 资料 ID |
| `relation_note` | VARCHAR(120) NULL | 关联备注，第一版前端可不填写 |
| `created_at` | DATETIME NOT NULL | 创建时间 |

索引与约束：

1. `UNIQUE KEY uk_post_catalog_relation (post_id, catalog_type, catalog_id)`，避免同一帖子重复关联同一资料。
2. `KEY idx_post_catalog_relation_catalog (catalog_type, catalog_id, created_at)`，用于资料详情页查询相关帖子。
3. `KEY idx_post_catalog_relation_post (post_id)`，用于帖子列表和详情批量回填资料标签。
4. `post_id` 外键关联 `post(id)`。
5. `catalog_id` 不做数据库外键，因为不同 `catalog_type` 对应不同资料表；由后端服务层校验资料是否存在且启用。

资料类型枚举：

| 枚举 | 资料表 | 前端路径 |
| --- | --- | --- |
| `MAP` | `tarkov_map` | `/catalog/maps/:id` |
| `TRADER` | `tarkov_trader` | `/catalog/traders/:id` |
| `QUEST` | `tarkov_quest` | `/catalog/quests/:id` |
| `ITEM` | `tarkov_item` | `/catalog/items/:id` |
| `WEAPON` | `tarkov_weapon` | `/catalog/weapons/:id` |
| `AMMO` | `tarkov_ammo` | `/catalog/ammo/:id` |
| `BOSS` | `boss` | `/catalog/bosses/:id` |
| `HIDEOUT` | `hideout_station` | `/catalog/hideout/:id` |

## 后端设计

### 新增实体与 Mapper

新增：

1. `PostCatalogRelation` 实体。
2. `PostCatalogRelationMapper`。

Mapper 需要支持：

1. 根据 `post_id` 批量查询关联资料。
2. 根据 `catalog_type + catalog_id` 分页查询相关帖子 ID 或关联记录。
3. 根据 `post_id` 删除旧关联，用于编辑帖子时重建关联。

### DTO

新增请求 DTO：

`PostCatalogRelationRequest`

字段：

1. `catalogType`
2. `catalogId`
3. `relationNote`

新增响应 DTO：

`RelatedCatalogResponse`

字段：

1. `catalogType`
2. `catalogId`
3. `name`
4. `subtitle`
5. `imageUrl`
6. `routeKind`
7. `relationNote`

扩展现有 DTO：

1. `PostCreateRequest` 增加 `List<PostCatalogRelationRequest> relations`。
2. `PostUpdateRequest` 增加 `List<PostCatalogRelationRequest> relations`。
3. `PostSummaryResponse` 增加 `List<RelatedCatalogResponse> relations`。
4. `PostDetailResponse` 增加 `List<RelatedCatalogResponse> relations`。

### 服务逻辑

发帖：

1. 校验分区仍沿用现有逻辑。
2. 创建帖子。
3. 校验并保存关联资料。
4. 如果关联资料中有不存在或停用的数据，返回 `400`。

编辑帖子：

1. 校验帖子归属仍沿用现有逻辑。
2. 更新帖子基础字段。
3. 删除旧关联。
4. 重建新关联。

帖子列表：

1. 保持现有分区、关键词、类型、推荐和排序筛选。
2. 新增可选参数：`catalogType`、`catalogId`。
3. 若传入资料筛选，则只返回关联该资料的帖子。
4. 返回帖子列表时批量回填每条帖子的资料标签，避免逐条查询。

帖子详情：

1. 保持现有浏览数、点赞、收藏状态逻辑。
2. 返回当前帖子的关联资料列表。

资料相关帖子：

复用帖子列表接口：

`GET /api/posts?catalogType=MAP&catalogId=1&page=1&size=5`

这样资料详情页不需要新建专用接口，前端可以直接调用现有帖子列表 API。

### 资料存在性校验

后端根据 `catalogType` 分派到对应 Mapper：

1. `MAP` -> `TarkovMapMapper`
2. `TRADER` -> `TarkovTraderMapper`
3. `QUEST` -> `TarkovQuestMapper`
4. `ITEM` -> `TarkovItemMapper`
5. `WEAPON` -> `TarkovWeaponMapper`
6. `AMMO` -> `TarkovAmmoMapper`
7. `BOSS` -> `BossMapper`
8. `HIDEOUT` -> `HideoutStationMapper`

校验原则：

1. 资料必须存在。
2. 有 `status` 字段的资料必须为 `ENABLED`。
3. `relations` 为空或不传时允许发帖。
4. 单个帖子最多关联 6 个资料，避免帖子卡片过度拥挤。
5. 同一资料重复提交时后端去重。

## 前端设计

### 资料中心页面

新增路由：

`/catalog`

页面目标：

1. 作为资料浏览主入口。
2. 分标签展示地图、商人、任务、武器、弹药、Boss、藏身处。
3. 提供关键词搜索。
4. 每个资料卡展示图片、名称、简要信息和“查看详情”入口。

布局风格：

1. 延续当前清爽社区风格。
2. 不做 Wiki 式密集表格。
3. 使用轻量卡片和明确分组。
4. 移动端保持单列或双列，不出现横向溢出。

### 发帖页关联资料选择

在发帖表单中新增“关联资料”区域：

1. 资料类型选择。
2. 资料搜索或下拉选择。
3. 已选择资料以标签/小卡片展示。
4. 可以删除已选资料。
5. 最多选择 6 个。

帖子类型与默认引导：

| 帖子类型 | 推荐资料类型 |
| --- | --- |
| `ROUTE` | 地图、Boss |
| `GUIDE` | 任务、商人、地图 |
| `LOADOUT` 或配装类 | 武器、弹药 |
| `MARKET` | 物品、商人 |
| `TEAM_UP` | 地图、Boss |
| `QUESTION` | 任意资料 |

如果现有帖子类型里没有 `LOADOUT`，本阶段可以先在 UI 中不新增类型，只对现有类型给出资料选择建议。

### 帖子列表与详情

帖子卡片：

1. 在标题下方展示最多 3 个关联资料标签。
2. 标签显示资料名称和类型。
3. 点击标签进入对应资料详情页。
4. 超过 3 个时显示“+N”。

帖子详情：

1. 在正文上方或右侧展示完整关联资料列表。
2. 每个关联资料显示图片缩略图、名称、类型和跳转入口。
3. 资料列表为空时不显示该区块。

### 资料详情页相关讨论

在 `CatalogDetailView` 增加“相关讨论”区：

1. 调用 `fetchPosts({ catalogType, catalogId, page: 1, size: 5 })`。
2. 展示帖子标题、摘要、作者、评论数、浏览数。
3. 提供“查看更多讨论”入口，跳转到 `/posts?catalogType=MAP&catalogId=1`。
4. 没有相关帖子时显示轻量空状态，提示用户发布第一条讨论。

### 论坛广场筛选

`PostBoardView` 增加从 URL 读取：

1. `catalogType`
2. `catalogId`

如果 URL 包含资料筛选：

1. 顶部显示当前资料筛选条件。
2. 支持清除资料筛选。
3. 仍保留关键词、分区、帖子类型、推荐、排序等原有筛选。

## 数据流

发帖流程：

1. 用户填写帖子。
2. 用户选择关联资料。
3. 前端提交 `relations`。
4. 后端创建帖子。
5. 后端校验资料并保存关联。
6. 帖子详情页返回关联资料摘要。

资料详情相关讨论流程：

1. 用户进入资料详情页。
2. 前端根据路由转换 `catalogType` 和 `catalogId`。
3. 前端调用帖子列表接口。
4. 后端根据 `post_catalog_relation` 查询相关帖子。
5. 前端展示相关讨论。

## 错误处理

1. 关联资料不存在：后端返回 `400`，提示“关联资料不存在或已停用”。
2. 关联资料数量超过 6 个：后端返回 `400`。
3. 重复关联：后端自动去重，不中断提交。
4. 资料详情相关讨论加载失败：前端不影响资料主体展示，只显示提示。
5. 未登录发帖：沿用现有登录跳转逻辑。

## 测试计划

后端测试：

1. 发帖时保存多条资料关联。
2. 编辑帖子时旧关联被替换。
3. 无效资料类型或不存在资料返回 `400`。
4. 单帖关联数量超过 6 个返回 `400`。
5. 帖子列表按 `catalogType + catalogId` 筛选。
6. 帖子列表和详情返回关联资料摘要。

前端验证：

1. 资料中心页面可切换资料类型并进入详情。
2. 发帖页可新增和删除关联资料。
3. 帖子列表展示资料标签且无布局溢出。
4. 帖子详情展示完整关联资料。
5. 资料详情页展示相关讨论。
6. 移动端检查资料中心、发帖页和帖子卡片布局。

## 实施顺序

1. 数据库表与实体 Mapper。
2. 后端 DTO、服务、接口测试。
3. 后端列表筛选和资料摘要回填。
4. 前端资料中心页面。
5. 发帖页关联资料选择。
6. 帖子列表和详情资料标签。
7. 资料详情页相关讨论。
8. 全量测试、浏览器验证、提交推送。

## 验收标准

1. 用户可以在发帖时关联多个塔科夫资料。
2. 帖子详情能看到所有关联资料，并能跳转到资料详情页。
3. 资料详情页能看到相关帖子。
4. 论坛广场能通过资料筛选帖子。
5. 后端测试通过。
6. 前端构建通过。
7. 首页、资料中心、帖子列表、帖子详情在桌面和移动端没有明显布局溢出。
