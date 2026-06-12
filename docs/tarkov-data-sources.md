# 塔科夫演示数据来源说明

本项目的演示数据用于课程展示和社区功能验证，不作为完整游戏百科使用。资料录入遵循“社区可读、来源可信、不过度堆叠”的原则。

## 资料来源

- Official Escape from Tarkov Wiki: 用于核对地图、Boss、任务、物品、弹药等基础资料结构，并为地图补充页面预览图链接。
- tarkov.dev API: 用于核对当前商人、地图、Boss 名称列表，并读取商人头像、Boss 头像、武器图片、弹药图片、弹药伤害、穿透和护甲伤害数值。
- 游戏内常见中文表达: 地图、物品、任务等保留中文展示；若无法确认官方中文名，则保留英文原名并使用中文说明补充语境。

## 图片与授权处理

- 数据库只保存外部图片 URL，不把 Wiki 或第三方图片文件下载进项目仓库。
- 地图图片优先使用 Official Escape from Tarkov Wiki 页面中对应地图的 showcase/preview 图片链接。
- 商人、Boss、武器和弹药图片优先使用 `https://assets.tarkov.dev/` 提供的结构化资源链接。
- 后续若图片失效，优先从 `tarkov.dev API` 或 Wiki 页面重新刷新 URL，而不是在代码中硬编码新的本地图片。

## 命名规则

- Boss 和商人: 页面展示统一使用英文原名，例如 `Prapor`、`Therapist`、`Reshala`、`Killa`。
- Boss 和商人数据库字段: `name_zh` 字段暂时保留兼容旧表结构；商人种子数据用英文原名占位，Boss 种子数据置空。
- 任务、地图、物品、武器、弹药、藏身处: 尽量保留中文名称，便于中文课程展示和玩家阅读。
- 帖子与评论: 内容采用玩家社区口吻，围绕任务路线、配装、市场、Boss、复盘和组队场景编写。

## 已补充的数据范围

- 地图: `Customs`、`Factory`、`Woods`、`Reserve`、`Streets of Tarkov`、`Shoreline`、`Interchange`、`Lighthouse`、`Ground Zero`、`The Lab`、`The Labyrinth`、`Terminal`、`Icebreaker`。
- 商人: `Prapor`、`Therapist`、`Skier`、`Mechanic`、`Ragman`、`Fence`、`Peacekeeper`、`Jaeger`、`Lightkeeper`、`Ref`、`BTR Driver`。
- Boss: `Reshala`、`Tagilla`、`Glukhar`、`Shturman`、`Sanitar`、`Killa`、`Kaban`、`Kollontay`、`Zryachiy`、`Knight`、`Big Pipe`、`Birdeye`、`Partisan`。
- 图片: 已为 13 张地图、11 名商人、13 个 Boss、8 把常用武器和 8 种常用弹药补充第一批图片链接。
- 社区内容: 已补充帖子、评论、点赞、收藏、关注关系、公告和部分帖子类型扩展表数据。

## 外部链接

- Official Escape from Tarkov Wiki: https://escapefromtarkov.fandom.com/wiki/Escape_from_Tarkov_Wiki
- Bosses: https://escapefromtarkov.fandom.com/wiki/Bosses
- Trading: https://escapefromtarkov.fandom.com/wiki/Trading
- Ballistics: https://escapefromtarkov.fandom.com/wiki/Ballistics
- tarkov.dev API: https://tarkov.dev/api/
