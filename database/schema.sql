CREATE DATABASE IF NOT EXISTS tarkov_community
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE tarkov_community;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(255) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  email VARCHAR(120) NULL,
  avatar VARCHAR(500) NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
  contribution INT NOT NULL DEFAULT 0,
  last_login_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_sys_user_username (username),
  UNIQUE KEY uk_sys_user_email (email),
  KEY idx_sys_user_role_status (role, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS user_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  bio VARCHAR(500) NULL,
  favorite_maps VARCHAR(255) NULL,
  play_style VARCHAR(80) NULL,
  server_region VARCHAR(80) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_profile_user_id (user_id),
  CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS user_follow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  followed_user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_follow_pair (user_id, followed_user_id),
  KEY idx_user_follow_user_id (user_id),
  KEY idx_user_follow_followed_user_id (followed_user_id),
  CONSTRAINT fk_user_follow_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_user_follow_followed_user FOREIGN KEY (followed_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS email_verification_token (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  token_hash VARCHAR(128) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  expires_at DATETIME NOT NULL,
  verified_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_email_verification_token_hash (token_hash),
  KEY idx_email_verification_user_status (user_id, status),
  CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS login_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  username VARCHAR(50) NOT NULL,
  ip VARCHAR(64) NULL,
  success TINYINT NOT NULL DEFAULT 0,
  message VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_login_log_user_id (user_id),
  KEY idx_login_log_created_at (created_at),
  CONSTRAINT fk_login_log_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  code VARCHAR(50) NOT NULL,
  description VARCHAR(255) NULL,
  icon VARCHAR(80) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_category_code (code),
  KEY idx_category_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  type VARCHAR(30) NOT NULL,
  color VARCHAR(30) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tag_name_type (name, type),
  KEY idx_tag_type_status (type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS tarkov_map (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name_en VARCHAR(80) NOT NULL,
  name_zh VARCHAR(80) NOT NULL,
  difficulty VARCHAR(30) NULL,
  description VARCHAR(500) NULL,
  recommended_level VARCHAR(50) NULL,
  image_url VARCHAR(500) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tarkov_map_name_en (name_en),
  KEY idx_tarkov_map_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS map_extract (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  map_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  faction_limit VARCHAR(40) NULL,
  condition_text VARCHAR(255) NULL,
  description VARCHAR(500) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_map_extract_map_id (map_id),
  CONSTRAINT fk_map_extract_map FOREIGN KEY (map_id) REFERENCES tarkov_map (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS map_loot_area (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  map_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  loot_type VARCHAR(80) NULL,
  risk_level VARCHAR(30) NULL,
  description VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_map_loot_area_map_id (map_id),
  KEY idx_map_loot_area_risk (risk_level),
  CONSTRAINT fk_map_loot_area_map FOREIGN KEY (map_id) REFERENCES tarkov_map (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS tarkov_trader (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name_en VARCHAR(80) NOT NULL,
  name_zh VARCHAR(80) NOT NULL,
  description VARCHAR(500) NULL,
  unlock_condition VARCHAR(255) NULL,
  avatar VARCHAR(500) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tarkov_trader_name_en (name_en),
  KEY idx_tarkov_trader_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS tarkov_quest (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  trader_id BIGINT NOT NULL,
  name_en VARCHAR(120) NOT NULL,
  name_zh VARCHAR(120) NULL,
  quest_type VARCHAR(50) NULL,
  map_id BIGINT NULL,
  description TEXT NULL,
  rewards VARCHAR(500) NULL,
  unlocks VARCHAR(500) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tarkov_quest_name_en (name_en),
  KEY idx_tarkov_quest_trader_id (trader_id),
  KEY idx_tarkov_quest_map_id (map_id),
  CONSTRAINT fk_tarkov_quest_trader FOREIGN KEY (trader_id) REFERENCES tarkov_trader (id),
  CONSTRAINT fk_tarkov_quest_map FOREIGN KEY (map_id) REFERENCES tarkov_map (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS quest_prerequisite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  quest_id BIGINT NOT NULL,
  prerequisite_quest_id BIGINT NOT NULL,
  UNIQUE KEY uk_quest_prerequisite_pair (quest_id, prerequisite_quest_id),
  CONSTRAINT fk_quest_prerequisite_quest FOREIGN KEY (quest_id) REFERENCES tarkov_quest (id),
  CONSTRAINT fk_quest_prerequisite_pre FOREIGN KEY (prerequisite_quest_id) REFERENCES tarkov_quest (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS tarkov_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name_en VARCHAR(120) NOT NULL,
  name_zh VARCHAR(120) NULL,
  item_type VARCHAR(60) NULL,
  rarity VARCHAR(40) NULL,
  grid_size VARCHAR(20) NULL,
  quest_needed TINYINT NOT NULL DEFAULT 0,
  hideout_needed TINYINT NOT NULL DEFAULT 0,
  keep_suggestion TINYINT NOT NULL DEFAULT 0,
  description VARCHAR(500) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tarkov_item_name_en (name_en),
  KEY idx_tarkov_item_type (item_type),
  KEY idx_tarkov_item_needed (quest_needed, hideout_needed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS tarkov_weapon (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name_en VARCHAR(120) NOT NULL,
  name_zh VARCHAR(120) NULL,
  weapon_type VARCHAR(60) NULL,
  caliber VARCHAR(60) NULL,
  description VARCHAR(500) NULL,
  image_url VARCHAR(500) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tarkov_weapon_name_en (name_en),
  KEY idx_tarkov_weapon_type (weapon_type),
  KEY idx_tarkov_weapon_caliber (caliber)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS tarkov_ammo (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name_en VARCHAR(120) NOT NULL,
  name_zh VARCHAR(120) NULL,
  caliber VARCHAR(60) NOT NULL,
  damage INT NULL,
  penetration INT NULL,
  armor_damage INT NULL,
  description VARCHAR(500) NULL,
  image_url VARCHAR(500) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tarkov_ammo_name_en (name_en),
  KEY idx_tarkov_ammo_caliber (caliber)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS hideout_station (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name_en VARCHAR(120) NOT NULL,
  name_zh VARCHAR(120) NULL,
  description VARCHAR(500) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_hideout_station_name_en (name_en)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS hideout_upgrade (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  station_id BIGINT NOT NULL,
  level INT NOT NULL,
  required_items TEXT NULL,
  required_time VARCHAR(80) NULL,
  unlocks VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_hideout_upgrade_station_level (station_id, level),
  CONSTRAINT fk_hideout_upgrade_station FOREIGN KEY (station_id) REFERENCES hideout_station (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS boss (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name_en VARCHAR(120) NOT NULL,
  name_zh VARCHAR(120) NULL,
  map_id BIGINT NULL,
  description VARCHAR(500) NULL,
  equipment_summary VARCHAR(500) NULL,
  image_url VARCHAR(500) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_boss_name_en (name_en),
  KEY idx_boss_map_id (map_id),
  CONSTRAINT fk_boss_map FOREIGN KEY (map_id) REFERENCES tarkov_map (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  title VARCHAR(160) NOT NULL,
  content TEXT NOT NULL,
  post_type VARCHAR(40) NOT NULL,
  cover_image VARCHAR(500) NULL,
  map_id BIGINT NULL,
  trader_id BIGINT NULL,
  quest_id BIGINT NULL,
  item_id BIGINT NULL,
  weapon_id BIGINT NULL,
  ammo_id BIGINT NULL,
  risk_level VARCHAR(30) NULL,
  intel_status VARCHAR(30) NULL,
  view_count INT NOT NULL DEFAULT 0,
  like_count INT NOT NULL DEFAULT 0,
  favorite_count INT NOT NULL DEFAULT 0,
  comment_count INT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
  pinned TINYINT NOT NULL DEFAULT 0,
  recommended TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_post_user_id (user_id),
  KEY idx_post_category_id (category_id),
  KEY idx_post_type_status_created (post_type, status, created_at),
  KEY idx_post_map_id (map_id),
  KEY idx_post_trader_id (trader_id),
  KEY idx_post_quest_id (quest_id),
  KEY idx_post_item_id (item_id),
  KEY idx_post_weapon_id (weapon_id),
  KEY idx_post_ammo_id (ammo_id),
  KEY idx_post_recommended_created (recommended, created_at),
  FULLTEXT KEY ft_post_title_content (title, content),
  CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_post_category FOREIGN KEY (category_id) REFERENCES category (id),
  CONSTRAINT fk_post_map FOREIGN KEY (map_id) REFERENCES tarkov_map (id),
  CONSTRAINT fk_post_trader FOREIGN KEY (trader_id) REFERENCES tarkov_trader (id),
  CONSTRAINT fk_post_quest FOREIGN KEY (quest_id) REFERENCES tarkov_quest (id),
  CONSTRAINT fk_post_item FOREIGN KEY (item_id) REFERENCES tarkov_item (id),
  CONSTRAINT fk_post_weapon FOREIGN KEY (weapon_id) REFERENCES tarkov_weapon (id),
  CONSTRAINT fk_post_ammo FOREIGN KEY (ammo_id) REFERENCES tarkov_ammo (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS post_catalog_relation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  catalog_type VARCHAR(30) NOT NULL,
  catalog_id BIGINT NOT NULL,
  relation_note VARCHAR(120),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_catalog_relation (post_id, catalog_type, catalog_id),
  KEY idx_post_catalog_relation_catalog (catalog_type, catalog_id, created_at),
  KEY idx_post_catalog_relation_post (post_id),
  CONSTRAINT fk_post_catalog_relation_post FOREIGN KEY (post_id) REFERENCES post (id)
);

CREATE TABLE IF NOT EXISTS post_tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_tag_pair (post_id, tag_id),
  KEY idx_post_tag_tag_id (tag_id),
  CONSTRAINT fk_post_tag_post FOREIGN KEY (post_id) REFERENCES post (id),
  CONSTRAINT fk_post_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS post_task_detail (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  quest_id BIGINT NULL,
  trader_id BIGINT NULL,
  map_id BIGINT NULL,
  task_type VARCHAR(60) NULL,
  required_items TEXT NULL,
  route_advice TEXT NULL,
  risk_level VARCHAR(30) NULL,
  intel_status VARCHAR(30) NULL,
  UNIQUE KEY uk_post_task_detail_post_id (post_id),
  CONSTRAINT fk_post_task_detail_post FOREIGN KEY (post_id) REFERENCES post (id),
  CONSTRAINT fk_post_task_detail_quest FOREIGN KEY (quest_id) REFERENCES tarkov_quest (id),
  CONSTRAINT fk_post_task_detail_trader FOREIGN KEY (trader_id) REFERENCES tarkov_trader (id),
  CONSTRAINT fk_post_task_detail_map FOREIGN KEY (map_id) REFERENCES tarkov_map (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS post_loadout_detail (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  weapon_id BIGINT NULL,
  ammo_id BIGINT NULL,
  armor_level VARCHAR(30) NULL,
  budget_level VARCHAR(30) NULL,
  suitable_maps VARCHAR(255) NULL,
  suitable_stage VARCHAR(60) NULL,
  loadout_summary TEXT NULL,
  UNIQUE KEY uk_post_loadout_detail_post_id (post_id),
  CONSTRAINT fk_post_loadout_detail_post FOREIGN KEY (post_id) REFERENCES post (id),
  CONSTRAINT fk_post_loadout_detail_weapon FOREIGN KEY (weapon_id) REFERENCES tarkov_weapon (id),
  CONSTRAINT fk_post_loadout_detail_ammo FOREIGN KEY (ammo_id) REFERENCES tarkov_ammo (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS post_market_detail (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  item_id BIGINT NULL,
  price_range VARCHAR(80) NULL,
  usage_type VARCHAR(60) NULL,
  suggestion VARCHAR(255) NULL,
  quest_needed TINYINT NOT NULL DEFAULT 0,
  hideout_needed TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_post_market_detail_post_id (post_id),
  CONSTRAINT fk_post_market_detail_post FOREIGN KEY (post_id) REFERENCES post (id),
  CONSTRAINT fk_post_market_detail_item FOREIGN KEY (item_id) REFERENCES tarkov_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS post_raid_review_detail (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  map_id BIGINT NULL,
  death_location VARCHAR(160) NULL,
  enemy_type VARCHAR(80) NULL,
  lost_equipment VARCHAR(255) NULL,
  injury_status VARCHAR(255) NULL,
  review_summary TEXT NULL,
  UNIQUE KEY uk_post_raid_review_detail_post_id (post_id),
  CONSTRAINT fk_post_raid_review_detail_post FOREIGN KEY (post_id) REFERENCES post (id),
  CONSTRAINT fk_post_raid_review_detail_map FOREIGN KEY (map_id) REFERENCES tarkov_map (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS post_teamup_detail (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  map_id BIGINT NULL,
  goal_type VARCHAR(60) NULL,
  team_size INT NULL,
  gear_requirement VARCHAR(255) NULL,
  voice_requirement VARCHAR(120) NULL,
  play_time VARCHAR(120) NULL,
  recruit_status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
  UNIQUE KEY uk_post_teamup_detail_post_id (post_id),
  CONSTRAINT fk_post_teamup_detail_post FOREIGN KEY (post_id) REFERENCES post (id),
  CONSTRAINT fk_post_teamup_detail_map FOREIGN KEY (map_id) REFERENCES tarkov_map (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS post_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  parent_id BIGINT NULL,
  reply_to_user_id BIGINT NULL,
  content TEXT NOT NULL,
  like_count INT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_post_comment_post_id_created (post_id, created_at),
  KEY idx_post_comment_user_id (user_id),
  KEY idx_post_comment_parent_id (parent_id),
  CONSTRAINT fk_post_comment_post FOREIGN KEY (post_id) REFERENCES post (id),
  CONSTRAINT fk_post_comment_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_post_comment_parent FOREIGN KEY (parent_id) REFERENCES post_comment (id),
  CONSTRAINT fk_post_comment_reply_user FOREIGN KEY (reply_to_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS post_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_like_pair (post_id, user_id),
  KEY idx_post_like_user_id (user_id),
  CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES post (id),
  CONSTRAINT fk_post_like_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS comment_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  comment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_comment_like_pair (comment_id, user_id),
  KEY idx_comment_like_user_id (user_id),
  CONSTRAINT fk_comment_like_comment FOREIGN KEY (comment_id) REFERENCES post_comment (id),
  CONSTRAINT fk_comment_like_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_favorite_pair (post_id, user_id),
  KEY idx_favorite_user_id (user_id),
  CONSTRAINT fk_favorite_post FOREIGN KEY (post_id) REFERENCES post (id),
  CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reporter_id BIGINT NOT NULL,
  target_type VARCHAR(20) NOT NULL,
  target_id BIGINT NOT NULL,
  reason VARCHAR(120) NOT NULL,
  description VARCHAR(500) NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  handler_id BIGINT NULL,
  handle_result VARCHAR(500) NULL,
  handled_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_report_once (reporter_id, target_type, target_id),
  KEY idx_report_status_created (status, created_at),
  KEY idx_report_handler_id (handler_id),
  CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES sys_user (id),
  CONSTRAINT fk_report_handler FOREIGN KEY (handler_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS notification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  type VARCHAR(40) NOT NULL,
  title VARCHAR(120) NOT NULL,
  content VARCHAR(500) NULL,
  related_id BIGINT NULL,
  read_status TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_notification_user_read_created (user_id, read_status, created_at),
  CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS announcement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(120) NOT NULL,
  content TEXT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_announcement_status_created (status, created_at),
  CONSTRAINT fk_announcement_creator FOREIGN KEY (created_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  admin_id BIGINT NOT NULL,
  action VARCHAR(80) NOT NULL,
  target_type VARCHAR(40) NULL,
  target_id BIGINT NULL,
  detail VARCHAR(500) NULL,
  ip VARCHAR(64) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_operation_log_admin_id (admin_id),
  KEY idx_operation_log_created_at (created_at),
  CONSTRAINT fk_operation_log_admin FOREIGN KEY (admin_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
