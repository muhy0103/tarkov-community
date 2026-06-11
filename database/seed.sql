USE tarkov_community;

INSERT INTO sys_user (id, username, password, nickname, email, role, status, contribution)
VALUES
  (1, 'admin', '$2a$10$fZQPznyJUYHn6Vr3DhaEYez/xqqLYYQyR/xqF1FaD0Gimpd0Rudgm', '社区管理员', 'admin@example.com', 'ADMIN', 'NORMAL', 100),
  (2, 'pmc_rookie', '$2b$10$KCd/lllnmSdJwet56sk5oOUycBL3oZxnoWJNjY3oG8SG785NaBo.6', '海关萌新', 'rookie@example.com', 'USER', 'NORMAL', 12),
  (3, 'reserve_runner', '$2b$10$KCd/lllnmSdJwet56sk5oOUycBL3oZxnoWJNjY3oG8SG785NaBo.6', '储备站跑商人', 'runner@example.com', 'USER', 'NORMAL', 36)
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), role = VALUES(role), status = VALUES(status);

INSERT INTO user_profile (user_id, bio, favorite_maps, play_style, server_region)
VALUES
  (1, '维护社区秩序与基础资料。', 'Customs,Reserve', '管理与审核', 'Asia'),
  (2, '正在学习任务路线和基础配装。', 'Customs,Woods', '任务优先', 'Asia'),
  (3, '喜欢跑商、复盘和整理市场物品。', 'Reserve,Streets of Tarkov', '跑商发育', 'Asia')
ON DUPLICATE KEY UPDATE bio = VALUES(bio), favorite_maps = VALUES(favorite_maps), play_style = VALUES(play_style);

INSERT INTO category (id, name, code, description, icon, sort_order, status)
VALUES
  (1, '战区地图', 'maps', '地图路线、撤离点、任务点位与高危区域讨论。', 'Map', 10, 'ENABLED'),
  (2, '任务档案', 'quests', '商人任务、任务物品与任务路线攻略。', 'NotebookTabs', 20, 'ENABLED'),
  (3, '装备弹药', 'loadouts', '武器改装、弹药选择与预算配装方案。', 'Shield', 30, 'ENABLED'),
  (4, '市场经济', 'market', '物资价值、跳蚤市场与藏身处材料讨论。', 'ChartLine', 40, 'ENABLED'),
  (5, '藏身处', 'hideout', '藏身处升级、材料收集与制作收益。', 'Warehouse', 50, 'ENABLED'),
  (6, '敌人与Boss', 'bosses', 'Boss、Scav、Rogue、Raider 与邪教徒情报。', 'Skull', 60, 'ENABLED'),
  (7, '战局复盘', 'raid-reviews', '死亡复盘、路线分析和战术讨论。', 'Crosshair', 70, 'ENABLED'),
  (8, '组队招募', 'team-up', '任务组队、跑图招募和语音开黑。', 'Users', 80, 'ENABLED')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), sort_order = VALUES(sort_order);

INSERT INTO tag (id, name, type, color, status)
VALUES
  (1, '新手向', 'PLAY_STYLE', '#22C55E', 'ENABLED'),
  (2, '任务攻略', 'SYSTEM', '#2563EB', 'ENABLED'),
  (3, '配装方案', 'SYSTEM', '#0EA5A4', 'ENABLED'),
  (4, '市场行情', 'SYSTEM', '#F59E0B', 'ENABLED'),
  (5, '战局复盘', 'SYSTEM', '#8B5CF6', 'ENABLED'),
  (6, '低风险', 'RISK', '#22C55E', 'ENABLED'),
  (7, '中风险', 'RISK', '#F59E0B', 'ENABLED'),
  (8, '高风险', 'RISK', '#EF4444', 'ENABLED'),
  (9, '已验证', 'SYSTEM', '#10B981', 'ENABLED'),
  (10, '版本待确认', 'SYSTEM', '#6B7280', 'ENABLED')
ON DUPLICATE KEY UPDATE color = VALUES(color), status = VALUES(status);

INSERT INTO tarkov_map (id, name_en, name_zh, difficulty, description, recommended_level, status)
VALUES
  (1, 'Customs', '海关', '中等', '新手任务密集，路线清晰但交战频繁。', '1+', 'ENABLED'),
  (2, 'Factory', '工厂', '高', '小地图、高交火密度，适合近距离战斗复盘。', '1+', 'ENABLED'),
  (3, 'Woods', '森林', '中等', '开阔区域多，适合任务和远距离路线规划。', '1+', 'ENABLED'),
  (4, 'Reserve', '储备站', '高', '高价值物资和复杂撤离条件并存。', '15+', 'ENABLED'),
  (5, 'Streets of Tarkov', '塔科夫街区', '高', '大型城区地图，点位复杂、资源丰富。', '15+', 'ENABLED')
ON DUPLICATE KEY UPDATE name_zh = VALUES(name_zh), difficulty = VALUES(difficulty), description = VALUES(description);

INSERT INTO tarkov_trader (id, name_en, name_zh, description, unlock_condition, status)
VALUES
  (1, 'Prapor', '普拉波', '早期任务和基础武器来源。', '默认解锁', 'ENABLED'),
  (2, 'Therapist', '治疗师', '医疗物资、钥匙和早期生存任务。', '默认解锁', 'ENABLED'),
  (3, 'Skier', '滑雪者', '装备交易与部分路线任务。', '默认解锁', 'ENABLED'),
  (4, 'Mechanic', '机械师', '武器改装与 Gunsmith 任务线。', '默认解锁', 'ENABLED'),
  (5, 'Ragman', '裁缝', '护甲、背包和服装相关任务。', '默认解锁', 'ENABLED')
ON DUPLICATE KEY UPDATE name_zh = VALUES(name_zh), description = VALUES(description);

INSERT INTO tarkov_quest (id, trader_id, name_en, name_zh, quest_type, map_id, description, rewards, unlocks, status)
VALUES
  (1, 1, 'Debut', '首秀', '击杀/交付', 1, '早期 Prapor 任务，要求击杀 Scav 并交付霰弹枪。', '经验、卢布、声望', '后续 Prapor 任务', 'ENABLED'),
  (2, 1, 'Delivery From the Past', '往事速递', '拾取/放置/撤离', 1, '跨海关和工厂的经典任务，路线风险较高。', '经验、卢布、声望', '后续任务链', 'ENABLED'),
  (3, 2, 'Shortage', '短缺', '交付', NULL, '交付 Salewa 急救包，适合新手学习医疗物资价值。', '经验、卢布、声望', '治疗师任务链', 'ENABLED'),
  (4, 4, 'Gunsmith Part 1', '枪匠 Part 1', '改枪', NULL, '根据要求改装 MP-133，适合展示配装讨论。', '经验、声望', 'Gunsmith 后续任务', 'ENABLED')
ON DUPLICATE KEY UPDATE name_zh = VALUES(name_zh), description = VALUES(description);

INSERT INTO tarkov_item (id, name_en, name_zh, item_type, rarity, grid_size, quest_needed, hideout_needed, keep_suggestion, description, status)
VALUES
  (1, 'Salewa first aid kit', 'Salewa 急救包', 'Medical', 'Common', '2x1', 1, 0, 1, '早期任务与战局治疗常用物品。', 'ENABLED'),
  (2, 'Secure Flash drive', '安全闪存盘', 'Electronics', 'Rare', '1x1', 1, 1, 1, '任务和藏身处常见需求物品。', 'ENABLED'),
  (3, 'Gas analyzer', '气体分析仪', 'Electronics', 'Rare', '1x2', 1, 1, 1, '早期任务关键物品。', 'ENABLED'),
  (4, 'Graphics card', '显卡', 'Electronics', 'Rare', '2x1', 0, 1, 1, '高价值物资，常用于经济讨论。', 'ENABLED')
ON DUPLICATE KEY UPDATE name_zh = VALUES(name_zh), quest_needed = VALUES(quest_needed), hideout_needed = VALUES(hideout_needed);

INSERT INTO tarkov_weapon (id, name_en, name_zh, weapon_type, caliber, description, status)
VALUES
  (1, 'AK-74N', 'AK-74N', 'Assault rifle', '5.45x39mm', '早中期常见突击步枪平台。', 'ENABLED'),
  (2, 'M4A1', 'M4A1', 'Assault rifle', '5.56x45mm', '改装空间高，适合中后期配装讨论。', 'ENABLED'),
  (3, 'MP-153', 'MP-153', 'Shotgun', '12/70', '早期任务和低成本近战武器。', 'ENABLED')
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO tarkov_ammo (id, name_en, name_zh, caliber, damage, penetration, armor_damage, description, status)
VALUES
  (1, '5.45x39mm BP', '5.45x39mm BP', '5.45x39mm', 48, 37, 41, '常见中期弹药示例。', 'ENABLED'),
  (2, '5.56x45mm M855A1', '5.56x45mm M855A1', '5.56x45mm', 49, 44, 52, 'M4 平台常见中高强度弹药示例。', 'ENABLED'),
  (3, '7.62x39mm PS', '7.62x39mm PS', '7.62x39mm', 57, 35, 52, '低成本常用弹药示例。', 'ENABLED')
ON DUPLICATE KEY UPDATE damage = VALUES(damage), penetration = VALUES(penetration);

INSERT INTO hideout_station (id, name_en, name_zh, description, status)
VALUES
  (1, 'Workbench', '工作台', '武器、弹药和任务物品制作模块。', 'ENABLED'),
  (2, 'Medstation', '医疗站', '医疗物资制作模块。', 'ENABLED'),
  (3, 'Lavatory', '厕所', '基础材料转换和部分任务物品制作模块。', 'ENABLED'),
  (4, 'Intelligence Center', '情报中心', '降低冷却、扩展收益与部分高级功能。', 'ENABLED')
ON DUPLICATE KEY UPDATE name_zh = VALUES(name_zh), description = VALUES(description);

INSERT INTO boss (id, name_en, name_zh, map_id, description, equipment_summary, status)
VALUES
  (1, 'Reshala', '雷舍拉', 1, '常见于海关，随从较多。', '突击步枪、护甲与随从装备。', 'ENABLED'),
  (2, 'Tagilla', '塔吉拉', 2, '工厂 Boss，近战压迫感强。', '大锤、护甲和近距离武器。', 'ENABLED'),
  (3, 'Glukhar', '格鲁哈', 4, '储备站 Boss，随从火力强。', '高强度护甲和突击武器。', 'ENABLED')
ON DUPLICATE KEY UPDATE name_zh = VALUES(name_zh), description = VALUES(description);
