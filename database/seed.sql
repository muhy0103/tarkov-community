USE tarkov_community;
-- Demo content references Official Escape from Tarkov Wiki and tarkov.dev data checked on 2026-06-12.

INSERT INTO sys_user (id, username, password, nickname, email, role, status, contribution)
VALUES
  (1, 'admin', '$2a$10$fZQPznyJUYHn6Vr3DhaEYez/xqqLYYQyR/xqF1FaD0Gimpd0Rudgm', '社区管理员', 'admin@example.com', 'ADMIN', 'NORMAL', 100),
  (2, 'pmc_rookie', '$2b$10$KCd/lllnmSdJwet56sk5oOUycBL3oZxnoWJNjY3oG8SG785NaBo.6', '海关萌新', 'rookie@example.com', 'USER', 'NORMAL', 12),
  (3, 'reserve_runner', '$2b$10$KCd/lllnmSdJwet56sk5oOUycBL3oZxnoWJNjY3oG8SG785NaBo.6', '储备站跑商人', 'runner@example.com', 'USER', 'NORMAL', 36),
  (4, 'woods_scout', '$2a$10$fZQPznyJUYHn6Vr3DhaEYez/xqqLYYQyR/xqF1FaD0Gimpd0Rudgm', '森林侦察员', 'woods@example.com', 'USER', 'NORMAL', 28),
  (5, 'labs_rat', '$2a$10$fZQPznyJUYHn6Vr3DhaEYez/xqqLYYQyR/xqF1FaD0Gimpd0Rudgm', '实验室仓鼠', 'labs@example.com', 'USER', 'NORMAL', 44)
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), role = VALUES(role), status = VALUES(status), contribution = VALUES(contribution);

INSERT INTO user_profile (user_id, bio, favorite_maps, play_style, server_region)
VALUES
  (1, '维护社区秩序与基础资料。', 'Customs,Reserve', '管理与审核', 'Asia'),
  (2, '正在学习任务路线和基础配装。', 'Customs,Woods', '任务优先', 'Asia'),
  (3, '喜欢跑商、复盘和整理市场物品。', 'Reserve,Streets of Tarkov', '跑商发育', 'Asia'),
  (4, '喜欢把任务点位和安全路线整理成清单。', 'Woods,Shoreline', '低风险任务', 'Asia'),
  (5, '偏好高风险收益路线，常记录实验室和街区复盘。', 'The Lab,Streets of Tarkov', '高风险复盘', 'Asia')
ON DUPLICATE KEY UPDATE bio = VALUES(bio), favorite_maps = VALUES(favorite_maps), play_style = VALUES(play_style);

INSERT INTO user_follow (id, user_id, followed_user_id)
VALUES
  (1, 2, 4),
  (2, 2, 5),
  (3, 3, 2),
  (4, 4, 2),
  (5, 5, 2),
  (6, 5, 4)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id), followed_user_id = VALUES(followed_user_id);

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
  (1, 'Customs', '海关', '中等', '早期任务密集，路线清晰但交战频繁，Dorms、Construction 与新加油站是常见冲突点。', '1+', 'ENABLED'),
  (2, 'Factory', '工厂', '高', '小型室内地图，交火节奏很快，适合近距离战斗、任务击杀和死亡复盘。', '1+', 'ENABLED'),
  (3, 'Woods', '森林', '中等', '开阔区域和树林遮蔽较多，适合远距离交火、任务路线和新手生存练习。', '1+', 'ENABLED'),
  (4, 'Reserve', '储备站', '高', '地下区域、装甲列车和地堡路线交织，高价值物资与复杂撤离条件并存。', '15+', 'ENABLED'),
  (5, 'Streets of Tarkov', '塔科夫街区', '高', '大型城区地图，建筑层次复杂，适合街区路线、Boss 情报和高价值物资讨论。', '15+', 'ENABLED'),
  (6, 'Shoreline', '海岸线', '中等', '疗养院、村庄和海岸线撤离点分布清晰，适合任务、钥匙房和中远距离路线。', '10+', 'ENABLED'),
  (7, 'Interchange', '立交桥', '高', '大型商场地图，商铺、地下停车场和电源机制会影响路线选择。', '10+', 'ENABLED'),
  (8, 'Lighthouse', '灯塔', '高', '水处理厂与 Rogue 区域风险很高，适合远距离路线和高风险收益讨论。', '15+', 'ENABLED'),
  (9, 'Ground Zero', '中心区', '中等', '面向低等级玩家的城市区域，任务点集中，适合新手路线教学。', '1+', 'ENABLED'),
  (10, 'The Lab', '实验室', '极高', '高风险室内地图，Raider、门禁卡和垂直空间让复盘价值很高。', '20+', 'ENABLED'),
  (11, 'The Labyrinth', '迷宫', '高', '高密度路线识别地图，适合记录房间标识、撤离方向和高压交火点。', '20+', 'ENABLED'),
  (12, 'Terminal', '码头', '待确认', '官方地图列表中的区域，作为后续资料扩展占位。', '待确认', 'ENABLED'),
  (13, 'Icebreaker', '破冰船', '待确认', 'tarkov.dev 当前地图数据中的区域，先作为资料扩展占位。', '待确认', 'ENABLED')
ON DUPLICATE KEY UPDATE name_zh = VALUES(name_zh), difficulty = VALUES(difficulty), description = VALUES(description), recommended_level = VALUES(recommended_level), status = VALUES(status);

INSERT INTO tarkov_trader (id, name_en, name_zh, description, unlock_condition, status)
VALUES
  (1, 'Prapor', 'Prapor', '早期任务、俄系武器、基础弹药与保险服务相关。', 'Available from the start', 'ENABLED'),
  (2, 'Therapist', 'Therapist', '医疗物资、钥匙、金钱兑换与早期生存任务相关。', 'Available from the start', 'ENABLED'),
  (3, 'Skier', 'Skier', '装备交易、部分西式配件和早期任务线相关。', 'Available from the start', 'ENABLED'),
  (4, 'Mechanic', 'Mechanic', '武器改装、电子物资和 Gunsmith 任务线相关。', 'Available from the start', 'ENABLED'),
  (5, 'Ragman', 'Ragman', '护甲、背包、胸挂、服装和商场任务线相关。', 'Available from the start', 'ENABLED'),
  (6, 'Fence', 'Fence', '特殊声望、Scav 相关交易和不稳定物资来源。', 'Available from the start', 'ENABLED'),
  (7, 'Peacekeeper', 'Peacekeeper', '美元交易、西式武器、北约弹药和海岸线任务相关。', 'Available from the start', 'ENABLED'),
  (8, 'Jaeger', 'Jaeger', '生存、狩猎、霰弹枪和森林任务线相关。', 'Unlocked through Introduction', 'ENABLED'),
  (9, 'Lightkeeper', 'Lightkeeper', '高等级任务链、灯塔区域和特殊解锁内容相关。', 'Unlocked through late-game questline', 'ENABLED'),
  (10, 'Ref', 'Ref', '竞技场联动与特殊交易相关。', 'Arena related', 'ENABLED'),
  (11, 'BTR Driver', 'BTR Driver', '地图内载具服务相关，适合做资料扩展。', 'Map service', 'ENABLED')
ON DUPLICATE KEY UPDATE name_zh = VALUES(name_zh), description = VALUES(description), unlock_condition = VALUES(unlock_condition), status = VALUES(status);

INSERT INTO tarkov_quest (id, trader_id, name_en, name_zh, quest_type, map_id, description, rewards, unlocks, status)
VALUES
  (1, 1, 'Debut', '首秀', '击杀/交付', 1, '早期 Prapor 任务，要求击杀 Scav 并交付霰弹枪。', '经验、卢布、声望', '后续 Prapor 任务', 'ENABLED'),
  (2, 1, 'Delivery From the Past', '往事速递', '拾取/放置/撤离', 1, '跨海关和工厂的经典任务，路线风险较高。', '经验、卢布、声望', '后续任务链', 'ENABLED'),
  (3, 2, 'Shortage', '短缺', '交付', NULL, '交付 Salewa 急救包，适合新手学习医疗物资价值。', '经验、卢布、声望', '治疗师任务链', 'ENABLED'),
  (4, 4, 'Gunsmith Part 1', '枪匠 Part 1', '改枪', NULL, '根据要求改装 MP-133，适合展示配装讨论。', '经验、声望', 'Gunsmith 后续任务', 'ENABLED'),
  (5, 8, 'Introduction', '介绍', '解锁', 3, '在森林地图找到 Jaeger 的营地信息并完成交付。', '经验、卢布、Jaeger 解锁', 'Jaeger 任务线', 'ENABLED'),
  (6, 2, 'Operation Aquarius Part 1', '水瓶座行动 Part 1', '地点确认', 1, '在海关宿舍区域确认水源相关地点，适合做宿舍路线说明。', '经验、卢布、声望', 'Operation Aquarius Part 2', 'ENABLED'),
  (7, 3, 'Supplier', '供应商', '交付', NULL, '交付护甲与霰弹枪，适合讨论早期装备保留策略。', '经验、卢布、声望', 'Skier 后续任务', 'ENABLED'),
  (8, 5, 'Big Sale', '大减价', '地点标记', 7, '在立交桥商场内标记多家商铺，路线规划价值高。', '经验、卢布、声望', 'Ragman 任务链', 'ENABLED'),
  (9, 7, 'Friend From the West Part 1', '西方朋友 Part 1', '击杀/交付', NULL, '面向 Peacekeeper 任务线的前置任务，常和 USEC 击杀讨论相关。', '经验、美元、声望', 'Friend From the West Part 2', 'ENABLED'),
  (10, 4, 'Farming Part 1', '农场 Part 1', '维修/放置', 2, '在工厂维修控制板，适合记录夜图路线和放置风险。', '经验、卢布、声望', 'Farming Part 2', 'ENABLED'),
  (11, 8, 'The Tarkov Shooter Part 1', '塔科夫射手 Part 1', '击杀', NULL, '使用栓动步枪完成距离要求，适合讨论低成本狙击路线。', '经验、卢布、声望', 'The Tarkov Shooter Part 2', 'ENABLED'),
  (12, 2, 'Colleagues Part 1', '同事 Part 1', '地点调查', 6, '在海岸线调查多个医疗相关地点，适合整理安全路线。', '经验、卢布、声望', 'Colleagues Part 2', 'ENABLED')
ON DUPLICATE KEY UPDATE name_zh = VALUES(name_zh), quest_type = VALUES(quest_type), map_id = VALUES(map_id), description = VALUES(description), rewards = VALUES(rewards), unlocks = VALUES(unlocks), status = VALUES(status);

INSERT INTO tarkov_item (id, name_en, name_zh, item_type, rarity, grid_size, quest_needed, hideout_needed, keep_suggestion, description, status)
VALUES
  (1, 'Salewa first aid kit', 'Salewa 急救包', 'Medical', 'Common', '2x1', 1, 0, 1, '早期任务与战局治疗常用物品。', 'ENABLED'),
  (2, 'Secure Flash drive', '安全闪存盘', 'Electronics', 'Rare', '1x1', 1, 1, 1, '任务和藏身处常见需求物品。', 'ENABLED'),
  (3, 'Gas analyzer', '气体分析仪', 'Electronics', 'Rare', '1x2', 1, 1, 1, '早期任务关键物品。', 'ENABLED'),
  (4, 'Graphics card', '显卡', 'Electronics', 'Rare', '2x1', 0, 1, 1, '高价值物资，常用于经济讨论。', 'ENABLED'),
  (5, 'Iskra ration pack', 'Iskra 军用口粮', 'Food', 'Common', '2x1', 1, 0, 1, '早期任务和能量补给常用物品。', 'ENABLED'),
  (6, 'Morphine injector', 'Morphine 注射器', 'Medical', 'Rare', '1x1', 1, 1, 1, '任务和战局应急止痛都常见。', 'ENABLED'),
  (7, 'Bolts', '螺栓', 'Building materials', 'Common', '1x1', 0, 1, 1, '藏身处早期升级常用材料。', 'ENABLED'),
  (8, 'Screw nuts', '螺母', 'Building materials', 'Common', '1x1', 0, 1, 1, '藏身处早期升级常用材料。', 'ENABLED'),
  (9, 'Car battery', '汽车电瓶', 'Energy elements', 'Rare', '3x2', 1, 1, 1, '任务和藏身处都可能需要，占格较大。', 'ENABLED'),
  (10, 'Corrugated hose', '波纹软管', 'Building materials', 'Rare', '2x1', 0, 1, 1, '藏身处升级需求常见，适合市场讨论。', 'ENABLED'),
  (11, 'Military cable', '军用电缆', 'Electronics', 'Rare', '2x1', 0, 1, 1, '高价值电子材料，常用于藏身处和交易讨论。', 'ENABLED'),
  (12, 'TerraGroup Labs keycard', 'TerraGroup 实验室门禁卡', 'Keycard', 'Rare', '1x1', 0, 0, 1, '进入实验室的关键物品，适合高风险路线讨论。', 'ENABLED')
ON DUPLICATE KEY UPDATE name_zh = VALUES(name_zh), item_type = VALUES(item_type), rarity = VALUES(rarity), grid_size = VALUES(grid_size), quest_needed = VALUES(quest_needed), hideout_needed = VALUES(hideout_needed), keep_suggestion = VALUES(keep_suggestion), description = VALUES(description), status = VALUES(status);

INSERT INTO tarkov_weapon (id, name_en, name_zh, weapon_type, caliber, description, status)
VALUES
  (1, 'AK-74N', 'AK-74N', 'Assault rifle', '5.45x39mm', '早中期常见突击步枪平台。', 'ENABLED'),
  (2, 'M4A1', 'M4A1', 'Assault rifle', '5.56x45mm', '改装空间高，适合中后期配装讨论。', 'ENABLED'),
  (3, 'MP-153', 'MP-153', 'Shotgun', '12/70', '早期任务和低成本近战武器。', 'ENABLED'),
  (4, 'AKS-74U', 'AKS-74U', 'Assault carbine', '5.45x39mm', '紧凑型 AK 平台，适合工厂和室内路线。', 'ENABLED'),
  (5, 'OP-SKS', 'OP-SKS', 'Carbine', '7.62x39mm', '低成本半自动平台，适合新手任务和中距离交火。', 'ENABLED'),
  (6, 'Mosin', 'Mosin', 'Bolt-action rifle', '7.62x54mm R', 'Jaeger 任务和低预算远距离练习常见。', 'ENABLED'),
  (7, 'PP-19-01 Vityaz', 'PP-19-01 Vityaz', 'Submachine gun', '9x19mm', '便宜稳定的 9x19 平台，适合前期配装讨论。', 'ENABLED'),
  (8, 'RFB', 'RFB', 'Battle rifle', '7.62x51mm', '中期常见 7.62x51 平台，适合穿甲弹药讨论。', 'ENABLED')
ON DUPLICATE KEY UPDATE name_zh = VALUES(name_zh), weapon_type = VALUES(weapon_type), caliber = VALUES(caliber), description = VALUES(description), status = VALUES(status);

INSERT INTO tarkov_ammo (id, name_en, name_zh, caliber, damage, penetration, armor_damage, description, status)
VALUES
  (1, '5.45x39mm BP gs', '5.45x39mm BP gs', '5.45x39mm', 48, 45, 46, '5.45 平台中期常见穿甲选择，数值来自 tarkov.dev。', 'ENABLED'),
  (2, '5.56x45mm M855A1', '5.56x45mm M855A1', '5.56x45mm', 49, 44, 47, '5.56 平台常见中高强度弹药，数值来自 tarkov.dev。', 'ENABLED'),
  (3, '7.62x39mm PS gzh', '7.62x39mm PS gzh', '7.62x39mm', 61, 35, 52, '低成本常用 7.62x39 弹药，数值来自 tarkov.dev。', 'ENABLED'),
  (4, '9x19mm Pst gzh', '9x19mm Pst gzh', '9x19mm', 54, 20, 33, '9x19 前期常见弹药，适合低预算配装。', 'ENABLED'),
  (5, '7.62x51mm M80', '7.62x51mm M80', '7.62x51mm', 80, 43, 67, '7.62x51 中期常见弹药，适合半自动步枪讨论。', 'ENABLED'),
  (6, '7.62x54mm R SNB gzh', '7.62x54mm R SNB gzh', '7.62x54mm R', 75, 62, 87, '高穿透栓动/精确射手弹药，适合远距离讨论。', 'ENABLED'),
  (7, '12/70 flechette', '12/70 flechette', '12/70', 25, 31, 26, '霰弹枪常见破甲弹种，适合近战配装讨论。', 'ENABLED'),
  (8, '.300 Blackout CBJ', '.300 Blackout CBJ', '.300 Blackout', 58, 43, 57, '.300 Blackout 高穿透选择，适合 MCX 配装讨论。', 'ENABLED')
ON DUPLICATE KEY UPDATE name_zh = VALUES(name_zh), caliber = VALUES(caliber), damage = VALUES(damage), penetration = VALUES(penetration), armor_damage = VALUES(armor_damage), description = VALUES(description), status = VALUES(status);

INSERT INTO hideout_station (id, name_en, name_zh, description, status)
VALUES
  (1, 'Workbench', '工作台', '武器、弹药和任务物品制作模块。', 'ENABLED'),
  (2, 'Medstation', '医疗站', '医疗物资制作模块。', 'ENABLED'),
  (3, 'Lavatory', '厕所', '基础材料转换和部分任务物品制作模块。', 'ENABLED'),
  (4, 'Intelligence Center', '情报中心', '降低冷却、扩展收益与部分高级功能。', 'ENABLED'),
  (5, 'Nutrition Unit', '营养部', '食物和饮品制作模块。', 'ENABLED'),
  (6, 'Shooting Range', '射击场', '测试后坐力、弹道和配装手感。', 'ENABLED'),
  (7, 'Bitcoin Farm', '比特币矿场', '高成本经济收益模块，适合市场收益讨论。', 'ENABLED')
ON DUPLICATE KEY UPDATE name_zh = VALUES(name_zh), description = VALUES(description);

INSERT INTO boss (id, name_en, name_zh, map_id, description, equipment_summary, status)
VALUES
  (1, 'Reshala', NULL, 1, 'Customs 常见 Boss，通常伴随多名护卫，Dorms 与新加油站周边需要重点确认。', '突击步枪、手枪、护甲与护卫装备。', 'ENABLED'),
  (2, 'Tagilla', NULL, 2, 'Factory 高压近战 Boss，机动性强，容易在室内转角突然压近。', '大锤、重型护甲、近距离武器。', 'ENABLED'),
  (3, 'Glukhar', NULL, 4, 'Reserve Boss，护卫数量多且火力强，地下区域和火车站附近风险高。', '突击步枪、精确射手武器、高级护甲与护卫装备。', 'ENABLED'),
  (4, 'Shturman', NULL, 3, 'Woods Boss，常围绕锯木厂活动，中远距离压制能力强。', '狙击步枪、护卫和高价值钥匙。', 'ENABLED'),
  (5, 'Sanitar', NULL, 6, 'Shoreline Boss，医疗主题明显，疗养院和海岸区域需要重点排查。', '医疗物资、护卫、突击武器。', 'ENABLED'),
  (6, 'Killa', NULL, 7, 'Interchange Boss，火力压制强，商场内部路线需要谨慎推进。', '机枪、重型护甲和高耐久头盔。', 'ENABLED'),
  (7, 'Kaban', NULL, 5, 'Streets of Tarkov Boss，周围区域防守强度高。', '重火力、护卫和街区固定点位装备。', 'ENABLED'),
  (8, 'Kollontay', NULL, 5, 'Streets of Tarkov Boss，近距离压迫和随从配合需要注意。', '警棍、护甲、随从武器。', 'ENABLED'),
  (9, 'Zryachiy', NULL, 8, 'Lighthouse 相关 Boss，灯塔区域风险极高。', '远距离武器和护卫支援。', 'ENABLED'),
  (10, 'Knight', NULL, 8, 'Goons 成员之一，可能在多个地图出现，适合记录刷新情报。', '高强度突击装备。', 'ENABLED'),
  (11, 'Big Pipe', NULL, 8, 'Goons 成员之一，常和 Knight、Birdeye 联动。', '榴弹发射器和突击装备。', 'ENABLED'),
  (12, 'Birdeye', NULL, 8, 'Goons 成员之一，远距离威胁高。', '精确射手武器和轻装机动装备。', 'ENABLED'),
  (13, 'Partisan', NULL, NULL, '可在多地图活动的威胁目标，适合社区持续记录。', '突击武器、特殊行为逻辑。', 'ENABLED')
ON DUPLICATE KEY UPDATE name_zh = VALUES(name_zh), map_id = VALUES(map_id), description = VALUES(description), equipment_summary = VALUES(equipment_summary), status = VALUES(status);

UPDATE tarkov_map
SET image_url = CASE name_en
  WHEN 'Customs' THEN 'https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/6/67/Customs_Showcase_1.png/revision/latest/scale-to-width-down/800?cb=20250316152159'
  WHEN 'Factory' THEN 'https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/f1/Factory_Showcase_1.png/revision/latest/scale-to-width-down/800?cb=20240825122753'
  WHEN 'Woods' THEN 'https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/4/46/Woods_Showcase_1.png/revision/latest/scale-to-width-down/800?cb=20230730012947'
  WHEN 'Reserve' THEN 'https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/0/02/Reserve_Showcase_1.png/revision/latest/scale-to-width-down/800?cb=20230730025742'
  WHEN 'Streets of Tarkov' THEN 'https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/c/c1/Streets_of_Tarkov_Showcase_1.png/revision/latest/scale-to-width-down/800?cb=20230903143522'
  WHEN 'Shoreline' THEN 'https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/d/dc/Shoreline_Showcase_1.png/revision/latest/scale-to-width-down/800?cb=20230730014748'
  WHEN 'Interchange' THEN 'https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/7/7d/Interchange_Showcase_1.png/revision/latest/scale-to-width-down/800?cb=20230730021446'
  WHEN 'Lighthouse' THEN 'https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/2/2e/Lighthouse_Showcase_1.png/revision/latest/scale-to-width-down/800?cb=20230730032105'
  WHEN 'Ground Zero' THEN 'https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/b/bc/Ground_Zero_Showcase_1.png/revision/latest/scale-to-width-down/800?cb=20240121131611'
  WHEN 'The Lab' THEN 'https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/5/5e/The_Lab_Showcase_1.png/revision/latest/scale-to-width-down/800?cb=20230730023546'
  WHEN 'The Labyrinth' THEN 'https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/b/be/The_Labyrinth_Transit_Map.png/revision/latest/scale-to-width-down/800?cb=20250328020348'
  WHEN 'Terminal' THEN 'https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/c/c0/Terminal_Showcase_1.jpg/revision/latest/scale-to-width-down/800?cb=20240826021257'
  WHEN 'Icebreaker' THEN 'https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/10/Icebreaker_Showcase_1.png/revision/latest/scale-to-width-down/800?cb=20260513144923'
  ELSE image_url
END
WHERE name_en IN ('Customs', 'Factory', 'Woods', 'Reserve', 'Streets of Tarkov', 'Shoreline', 'Interchange', 'Lighthouse', 'Ground Zero', 'The Lab', 'The Labyrinth', 'Terminal', 'Icebreaker');

UPDATE tarkov_trader
SET avatar = CASE name_en
  WHEN 'Prapor' THEN 'https://assets.tarkov.dev/54cb50c76803fa8b248b4571.webp'
  WHEN 'Therapist' THEN 'https://assets.tarkov.dev/54cb57776803fa99248b456e.webp'
  WHEN 'Skier' THEN 'https://assets.tarkov.dev/58330581ace78e27b8b10cee.webp'
  WHEN 'Mechanic' THEN 'https://assets.tarkov.dev/5a7c2eca46aef81a7ca2145d.webp'
  WHEN 'Ragman' THEN 'https://assets.tarkov.dev/5ac3b934156ae10c4430e83c.webp'
  WHEN 'Fence' THEN 'https://assets.tarkov.dev/579dc571d53a0658a154fbec.webp'
  WHEN 'Peacekeeper' THEN 'https://assets.tarkov.dev/5935c25fb3acc3127c3d8cd9.webp'
  WHEN 'Jaeger' THEN 'https://assets.tarkov.dev/5c0647fdd443bc2504c2d371.webp'
  WHEN 'Lightkeeper' THEN 'https://assets.tarkov.dev/638f541a29ffd1183d187f57.webp'
  WHEN 'Ref' THEN 'https://assets.tarkov.dev/6617beeaa9cfa777ca915b7c.webp'
  WHEN 'BTR Driver' THEN 'https://assets.tarkov.dev/656f0f98d80a697f855d34b1.webp'
  ELSE avatar
END
WHERE name_en IN ('Prapor', 'Therapist', 'Skier', 'Mechanic', 'Ragman', 'Fence', 'Peacekeeper', 'Jaeger', 'Lightkeeper', 'Ref', 'BTR Driver');

UPDATE tarkov_weapon
SET image_url = CASE name_en
  WHEN 'AK-74N' THEN 'https://assets.tarkov.dev/5644bd2b4bdc2d3b4c8b4572-512.webp'
  WHEN 'M4A1' THEN 'https://assets.tarkov.dev/5447a9cd4bdc2dbd208b4567-512.webp'
  WHEN 'MP-153' THEN 'https://assets.tarkov.dev/56dee2bdd2720bc8328b4567-512.webp'
  WHEN 'AKS-74U' THEN 'https://assets.tarkov.dev/57dc2fa62459775949412633-512.webp'
  WHEN 'OP-SKS' THEN 'https://assets.tarkov.dev/587e02ff24597743df3deaeb-512.webp'
  WHEN 'Mosin' THEN 'https://assets.tarkov.dev/5ae08f0a5acfc408fb1398a1-512.webp'
  WHEN 'PP-19-01 Vityaz' THEN 'https://assets.tarkov.dev/59984ab886f7743e98271174-512.webp'
  WHEN 'RFB' THEN 'https://assets.tarkov.dev/5f2a9575926fd9352339381f-512.webp'
  ELSE image_url
END
WHERE name_en IN ('AK-74N', 'M4A1', 'MP-153', 'AKS-74U', 'OP-SKS', 'Mosin', 'PP-19-01 Vityaz', 'RFB');

UPDATE tarkov_ammo
SET image_url = CASE name_en
  WHEN '5.45x39mm BP gs' THEN 'https://assets.tarkov.dev/56dfef82d2720bbd668b4567-512.webp'
  WHEN '5.45x39mm BP' THEN 'https://assets.tarkov.dev/56dfef82d2720bbd668b4567-512.webp'
  WHEN '5.56x45mm M855A1' THEN 'https://assets.tarkov.dev/54527ac44bdc2d36668b4567-512.webp'
  WHEN '7.62x39mm PS gzh' THEN 'https://assets.tarkov.dev/5656d7c34bdc2d9d198b4587-512.webp'
  WHEN '7.62x39mm PS' THEN 'https://assets.tarkov.dev/5656d7c34bdc2d9d198b4587-512.webp'
  WHEN '9x19mm Pst gzh' THEN 'https://assets.tarkov.dev/56d59d3ad2720bdb418b4577-512.webp'
  WHEN '7.62x51mm M80' THEN 'https://assets.tarkov.dev/58dd3ad986f77403051cba8f-512.webp'
  WHEN '7.62x54mm R SNB gzh' THEN 'https://assets.tarkov.dev/560d61e84bdc2da74d8b4571-512.webp'
  WHEN '12/70 flechette' THEN 'https://assets.tarkov.dev/5d6e6911a4b9361bd5780d52-512.webp'
  WHEN '.300 Blackout CBJ' THEN 'https://assets.tarkov.dev/64b8725c4b75259c590fa899-512.webp'
  ELSE image_url
END
WHERE name_en IN ('5.45x39mm BP gs', '5.45x39mm BP', '5.56x45mm M855A1', '7.62x39mm PS gzh', '7.62x39mm PS', '9x19mm Pst gzh', '7.62x51mm M80', '7.62x54mm R SNB gzh', '12/70 flechette', '.300 Blackout CBJ');

UPDATE boss
SET image_url = CASE name_en
  WHEN 'Reshala' THEN 'https://assets.tarkov.dev/reshala-portrait.webp'
  WHEN 'Tagilla' THEN 'https://assets.tarkov.dev/tagilla-portrait.png'
  WHEN 'Glukhar' THEN 'https://assets.tarkov.dev/glukhar-portrait.png'
  WHEN 'Shturman' THEN 'https://assets.tarkov.dev/shturman-portrait.png'
  WHEN 'Sanitar' THEN 'https://assets.tarkov.dev/sanitar-portrait.png'
  WHEN 'Killa' THEN 'https://assets.tarkov.dev/killa-portrait.png'
  WHEN 'Kaban' THEN 'https://assets.tarkov.dev/kaban-portrait.png'
  WHEN 'Kollontay' THEN 'https://assets.tarkov.dev/kollontay-portrait.png'
  WHEN 'Zryachiy' THEN 'https://assets.tarkov.dev/zryachiy-portrait.png'
  WHEN 'Knight' THEN 'https://assets.tarkov.dev/knight-portrait.png'
  WHEN 'Big Pipe' THEN 'https://assets.tarkov.dev/big-pipe-portrait.png'
  WHEN 'Birdeye' THEN 'https://assets.tarkov.dev/birdeye-portrait.png'
  WHEN 'Partisan' THEN 'https://assets.tarkov.dev/partisan-portrait.png'
  ELSE image_url
END
WHERE name_en IN ('Reshala', 'Tagilla', 'Glukhar', 'Shturman', 'Sanitar', 'Killa', 'Kaban', 'Kollontay', 'Zryachiy', 'Knight', 'Big Pipe', 'Birdeye', 'Partisan');

INSERT INTO post (id, user_id, category_id, title, content, post_type, cover_image, map_id, trader_id, quest_id, item_id, weapon_id, ammo_id, risk_level, intel_status, view_count, like_count, favorite_count, comment_count, status, pinned, recommended, created_at)
VALUES
  (1, 2, 1, '海关新手任务路线：避开 Dorms 正面交火', '这条路线适合刚开始做 Prapor 和 Therapist 任务的玩家。出生在 Trailer Park 一侧时，先确认 Big Red 周边声音，再沿外墙绕向河道；如果 Dorms 已经有枪声，不建议直接穿中路，可以先走 Power line 附近确认 Scav，再从 ZB-1011 方向撤离。重点是不要贪 Dorms，先把任务物品带出去。', 'ROUTE', NULL, 1, 1, 1, NULL, NULL, NULL, 'LOW', 'VERIFIED', 186, 12, 5, 2, 'NORMAL', 1, 1, '2026-06-05 20:12:00'),
  (2, 4, 2, 'Delivery From the Past 低风险分段思路', '这个任务最容易翻车的点是 Factory 放置阶段。建议第一局只在 Customs 拿文件并安全撤离，第二局 Factory 单独处理放置。Factory 里尽量等第一波枪声结束再移动，放置前确认楼梯和办公室走廊，带一颗闪光弹会舒服很多。', 'GUIDE', NULL, 1, 1, 2, NULL, NULL, NULL, 'MEDIUM', 'VERIFIED', 142, 9, 4, 2, 'NORMAL', 0, 1, '2026-06-06 16:40:00'),
  (3, 3, 4, '显卡和军用电缆：什么时候卖，什么时候留', '显卡和 Military cable 都属于高价值物资。没有急需现金时建议先看藏身处进度：显卡优先考虑后续经济模块，军用电缆则根据 Intelligence Center 和后续升级需求保留。市场价格高位时可以分批出，不建议背包满了就立刻卖。', 'MARKET', NULL, NULL, NULL, NULL, 4, NULL, NULL, 'MEDIUM', 'PENDING', 98, 7, 3, 1, 'NORMAL', 0, 1, '2026-06-07 21:18:00'),
  (4, 5, 7, '实验室第一次进图复盘：不要急着开高价值房', '第一次进 The Lab 最大问题是信息过载。我的建议是先熟悉电梯、地下和主要楼梯，不要第一时间冲高价值房。听到 Raider 语音后先确认方向，尽量让路线有退路。门禁卡成本已经很高，目标应该是活着带出信息，而不是第一把就赚钱。', 'QUESTION', NULL, 10, NULL, NULL, 12, NULL, NULL, 'HIGH', 'PENDING', 231, 16, 8, 2, 'NORMAL', 0, 1, '2026-06-08 22:05:00'),
  (5, 4, 3, '森林新手低预算：Mosin + SNB 是否值得', 'Mosin 的优势是路线简单、射击节奏清楚，适合练习距离判断。SNB 穿透很高，但对新手来说弹药成本压力也大。如果只是做 The Tarkov Shooter，可以先用更便宜的弹药练手，熟悉锯木厂外圈和岩石点位后再升级弹药。', 'LOADOUT', NULL, 3, 8, 11, NULL, 6, 6, 'MEDIUM', 'VERIFIED', 117, 8, 2, 1, 'NORMAL', 0, 0, '2026-06-09 19:33:00'),
  (6, 2, 6, 'Factory 遇到 Tagilla 的处理方式', 'Tagilla 的压迫感来自突然贴近和高护甲。听到快速脚步或近距离锤击音时，不要在狭窄通道原地架枪，优先拉开距离或换到有掩体的位置。腿部和连续压制更现实，单人硬拼很容易被节奏带走。', 'GUIDE', NULL, 2, NULL, NULL, NULL, 3, 7, 'HIGH', 'VERIFIED', 168, 10, 4, 2, 'NORMAL', 0, 1, '2026-06-09 23:21:00'),
  (7, 3, 5, 'Workbench 早期升级材料清单', 'Workbench 相关材料建议提前留 Bolts、Screw nuts、波纹软管和常见工具。不要只看单件价格，很多材料在做任务或升级时会同时缺。跑 Reserve 或 Interchange 时可以专门留一个小包位给建筑材料。', 'MARKET', NULL, NULL, NULL, NULL, 7, NULL, NULL, 'LOW', 'VERIFIED', 75, 5, 2, 1, 'NORMAL', 0, 0, '2026-06-10 13:45:00'),
  (8, 5, 1, 'Streets 跑商路线：先定撤离，再定战利品', 'Streets of Tarkov 的核心不是哪栋楼最肥，而是你能不能把路线闭环。建议进图后先确认撤离，再决定是否进高价值建筑。Kaban 周边和主干道枪声密集时，不要把背包塞满后再找路，负重会让撤离风险明显上升。', 'ROUTE', NULL, 5, NULL, NULL, NULL, NULL, NULL, 'HIGH', 'PENDING', 204, 15, 7, 2, 'NORMAL', 0, 1, '2026-06-10 22:50:00'),
  (9, 2, 8, '今晚海岸线任务队，主打 Colleagues Part 1', '目标是 Shoreline 跑医疗相关点位，优先安全撤离，不打疗养院正面。希望队友能开麦、带基础医疗和一套中低成本装备。时间 21:30 到 23:00，服务器 Asia。', 'TEAM_UP', NULL, 6, 2, 12, NULL, NULL, NULL, 'MEDIUM', 'VERIFIED', 62, 4, 1, 1, 'NORMAL', 0, 0, '2026-06-11 18:20:00'),
  (10, 4, 2, 'Big Sale 商铺标记顺序建议', 'Interchange 做 Big Sale 时不要一进商场就冲中心区域。可以先从靠近出生点的一侧商铺开始，按外圈到中圈的顺序推进。电源开启后玩家路线会变化，听到电梯和地下枪声时建议暂缓标记。', 'GUIDE', NULL, 7, 5, 8, NULL, NULL, NULL, 'MEDIUM', 'VERIFIED', 86, 6, 2, 1, 'NORMAL', 0, 0, '2026-06-11 20:10:00')
ON DUPLICATE KEY UPDATE title = VALUES(title), content = VALUES(content), post_type = VALUES(post_type), map_id = VALUES(map_id), trader_id = VALUES(trader_id), quest_id = VALUES(quest_id), item_id = VALUES(item_id), weapon_id = VALUES(weapon_id), ammo_id = VALUES(ammo_id), risk_level = VALUES(risk_level), intel_status = VALUES(intel_status), view_count = VALUES(view_count), like_count = VALUES(like_count), favorite_count = VALUES(favorite_count), comment_count = VALUES(comment_count), status = VALUES(status), pinned = VALUES(pinned), recommended = VALUES(recommended);

INSERT INTO post_task_detail (post_id, quest_id, trader_id, map_id, task_type, required_items, route_advice, risk_level, intel_status)
VALUES
  (2, 2, 1, 1, '拾取/放置/撤离', '任务文件、基础医疗、闪光弹', 'Customs 拿文件和 Factory 放置分开做，减少连续失败成本。', 'MEDIUM', 'VERIFIED'),
  (9, 12, 2, 6, '地点调查', '基础医疗、止痛、备用食物', '先跑外圈医疗点，再判断是否进疗养院附近。', 'MEDIUM', 'VERIFIED'),
  (10, 8, 5, 7, '地点标记', '标记器、止痛、轻装护甲', '外圈商铺到中圈商铺推进，避开开局中心交火。', 'MEDIUM', 'VERIFIED')
ON DUPLICATE KEY UPDATE quest_id = VALUES(quest_id), trader_id = VALUES(trader_id), map_id = VALUES(map_id), task_type = VALUES(task_type), required_items = VALUES(required_items), route_advice = VALUES(route_advice), risk_level = VALUES(risk_level), intel_status = VALUES(intel_status);

INSERT INTO post_loadout_detail (post_id, weapon_id, ammo_id, armor_level, budget_level, suitable_maps, suitable_stage, loadout_summary)
VALUES
  (5, 6, 6, '3-4级', '中等', 'Woods,Shoreline', '任务期', 'Mosin 适合训练距离判断，SNB 穿透强但成本较高。'),
  (6, 3, 7, '3-4级', '低到中等', 'Factory', '早中期', '霰弹枪配 flechette 适合应对高护甲近距离目标。')
ON DUPLICATE KEY UPDATE weapon_id = VALUES(weapon_id), ammo_id = VALUES(ammo_id), armor_level = VALUES(armor_level), budget_level = VALUES(budget_level), suitable_maps = VALUES(suitable_maps), suitable_stage = VALUES(suitable_stage), loadout_summary = VALUES(loadout_summary);

INSERT INTO post_market_detail (post_id, item_id, price_range, usage_type, suggestion, quest_needed, hideout_needed)
VALUES
  (3, 4, '高波动', '藏身处/出售', '没急需现金时优先看藏身处进度，价格高位可分批出售。', 0, 1),
  (7, 7, '低到中等', '藏身处材料', '早期多留几组，避免升级时临时高价购买。', 0, 1)
ON DUPLICATE KEY UPDATE item_id = VALUES(item_id), price_range = VALUES(price_range), usage_type = VALUES(usage_type), suggestion = VALUES(suggestion), quest_needed = VALUES(quest_needed), hideout_needed = VALUES(hideout_needed);

INSERT INTO post_raid_review_detail (post_id, map_id, death_location, enemy_type, lost_equipment, injury_status, review_summary)
VALUES
  (4, 10, '主楼地下转角', 'Raider', '中等护甲、M4A1、基础医疗', '胸部重伤后撤离失败', '首次实验室建议先学习撤离与楼梯，不要开局冲高价值房。')
ON DUPLICATE KEY UPDATE map_id = VALUES(map_id), death_location = VALUES(death_location), enemy_type = VALUES(enemy_type), lost_equipment = VALUES(lost_equipment), injury_status = VALUES(injury_status), review_summary = VALUES(review_summary);

INSERT INTO post_teamup_detail (post_id, map_id, goal_type, team_size, gear_requirement, voice_requirement, play_time, recruit_status)
VALUES
  (9, 6, '任务路线', 3, '中低成本装备，带基础医疗', '语音沟通', '21:30-23:00', 'OPEN')
ON DUPLICATE KEY UPDATE map_id = VALUES(map_id), goal_type = VALUES(goal_type), team_size = VALUES(team_size), gear_requirement = VALUES(gear_requirement), voice_requirement = VALUES(voice_requirement), play_time = VALUES(play_time), recruit_status = VALUES(recruit_status);

INSERT INTO post_tag (id, post_id, tag_id)
VALUES
  (1, 1, 1), (2, 1, 2), (3, 1, 9),
  (4, 2, 2), (5, 2, 7), (6, 2, 9),
  (7, 3, 4), (8, 3, 10),
  (9, 4, 5), (10, 4, 8),
  (11, 5, 3), (12, 6, 8), (13, 7, 4),
  (14, 8, 8), (15, 9, 7), (16, 10, 2)
ON DUPLICATE KEY UPDATE post_id = VALUES(post_id), tag_id = VALUES(tag_id);

INSERT INTO post_comment (id, post_id, user_id, parent_id, reply_to_user_id, content, like_count, status, created_at)
VALUES
  (1, 1, 4, NULL, NULL, 'Dorms 有枪声时先绕确实更稳，新手最怕为了一个点位把任务物品送掉。', 3, 'NORMAL', '2026-06-05 21:02:00'),
  (2, 1, 3, NULL, NULL, '如果从 RUAF 一侧出生，也可以先看河道再决定要不要进建筑区。', 2, 'NORMAL', '2026-06-05 21:18:00'),
  (3, 2, 2, NULL, NULL, 'Factory 分开做很有用，我之前连续两次都是拿完文件直接进工厂翻车。', 4, 'NORMAL', '2026-06-06 17:10:00'),
  (4, 2, 5, 3, 2, '可以考虑夜图，带手电但不要一直开。', 1, 'NORMAL', '2026-06-06 17:24:00'),
  (5, 3, 4, NULL, NULL, '显卡我一般先留，等藏身处方向确定后再卖。', 2, 'NORMAL', '2026-06-07 22:01:00'),
  (6, 4, 3, NULL, NULL, '实验室第一目标确实应该是活着带出路线信息，收益可以后面再追。', 5, 'NORMAL', '2026-06-08 22:33:00'),
  (7, 4, 2, NULL, NULL, '可以先离线或看地图熟悉电梯，实际进图时压力会小很多。', 3, 'NORMAL', '2026-06-08 22:48:00'),
  (8, 5, 2, NULL, NULL, 'SNB 很强，但新手更重要的是知道什么时候该开枪，什么时候该走。', 2, 'NORMAL', '2026-06-09 20:02:00'),
  (9, 6, 5, NULL, NULL, '打 Tagilla 不要贪头，拉距离和打腿更现实。', 3, 'NORMAL', '2026-06-09 23:50:00'),
  (10, 6, 4, NULL, NULL, 'Factory 转角多，提前听脚步比硬架更重要。', 1, 'NORMAL', '2026-06-10 00:08:00'),
  (11, 7, 2, NULL, NULL, 'Bolts 和螺母是真的不嫌多，前期经常临时缺。', 1, 'NORMAL', '2026-06-10 14:20:00'),
  (12, 8, 4, NULL, NULL, 'Streets 背包重了之后很容易撤离失败，路线闭环比贪更重要。', 4, 'NORMAL', '2026-06-10 23:11:00'),
  (13, 8, 3, NULL, NULL, 'Kaban 周边建议先听枪声，没必要第一时间靠近。', 2, 'NORMAL', '2026-06-10 23:30:00'),
  (14, 9, 4, NULL, NULL, '我可以来，主跑任务，不主动打疗养院。', 1, 'NORMAL', '2026-06-11 18:45:00'),
  (15, 10, 2, NULL, NULL, 'Big Sale 先外圈后中圈很稳，尤其是单排。', 2, 'NORMAL', '2026-06-11 20:44:00')
ON DUPLICATE KEY UPDATE content = VALUES(content), like_count = VALUES(like_count), status = VALUES(status);

INSERT INTO post_like (id, post_id, user_id, created_at)
VALUES
  (1, 1, 3, '2026-06-05 21:05:00'), (2, 1, 4, '2026-06-05 21:09:00'), (3, 1, 5, '2026-06-05 21:15:00'),
  (4, 2, 2, '2026-06-06 17:12:00'), (5, 2, 5, '2026-06-06 17:20:00'),
  (6, 3, 4, '2026-06-07 22:10:00'), (7, 4, 3, '2026-06-08 22:36:00'), (8, 4, 4, '2026-06-08 22:40:00'),
  (9, 5, 2, '2026-06-09 20:05:00'), (10, 6, 5, '2026-06-09 23:55:00'),
  (11, 8, 2, '2026-06-10 23:13:00'), (12, 8, 3, '2026-06-10 23:20:00'), (13, 9, 4, '2026-06-11 18:48:00')
ON DUPLICATE KEY UPDATE created_at = VALUES(created_at);

INSERT INTO favorite (id, post_id, user_id, created_at)
VALUES
  (1, 1, 3, '2026-06-05 21:20:00'), (2, 1, 4, '2026-06-05 21:21:00'),
  (3, 2, 2, '2026-06-06 17:28:00'), (4, 3, 4, '2026-06-07 22:12:00'),
  (5, 4, 3, '2026-06-08 22:52:00'), (6, 8, 2, '2026-06-10 23:32:00'),
  (7, 8, 4, '2026-06-10 23:38:00')
ON DUPLICATE KEY UPDATE created_at = VALUES(created_at);

INSERT INTO announcement (id, title, content, status, created_by, created_at)
VALUES
  (1, '社区资料维护说明', 'Boss 和商人名称统一按英文原名展示，地图、任务、物品等资料尽量使用游戏内中文或可信资料来源。', 'PUBLISHED', 1, '2026-06-05 10:00:00'),
  (2, '情报帖推荐规范', '推荐帖优先展示路线清楚、风险说明完整、能被其他玩家复现或补充的内容。', 'PUBLISHED', 1, '2026-06-06 10:00:00'),
  (3, '演示数据已补充', '当前数据库包含地图、任务、弹药、Boss、帖子、评论和互动样例，便于课程展示。', 'PUBLISHED', 1, '2026-06-12 09:50:00')
ON DUPLICATE KEY UPDATE title = VALUES(title), content = VALUES(content), status = VALUES(status), created_by = VALUES(created_by);
