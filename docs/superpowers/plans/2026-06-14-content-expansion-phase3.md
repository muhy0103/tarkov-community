# Content Expansion Phase 3 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expand the Tarkov community demo data so the site feels like a richer player community and catalog, while keeping the existing clean UI and database schema.

**Architecture:** Add a standalone phase-3 SQL seed file instead of rewriting the original seed heavily. Import the file into local MySQL, verify catalog counts and rendered pages, then document the expanded data scope.

**Tech Stack:** MySQL seed SQL, Spring Boot existing catalog APIs, Vue 3 frontend, local static media already under `frontend/public/assets/catalog/`.

---

### Task 1: Generate Phase-3 Data Seed

**Files:**
- Create: `database/seed-content-phase3.sql`
- Modify: `docs/tarkov-data-sources.md`

- [ ] **Step 1: Build a SQL seed block**

Create SQL that inserts additional enabled rows with stable IDs:

```sql
USE tarkov_community;

INSERT INTO tarkov_quest (...) VALUES (...);
INSERT INTO tarkov_item (...) VALUES (...);
INSERT INTO tarkov_weapon (...) VALUES (...);
INSERT INTO tarkov_ammo (...) VALUES (...);
INSERT INTO map_extract (...) VALUES (...);
INSERT INTO map_loot_area (...) VALUES (...);
INSERT INTO hideout_station (...) VALUES (...);
INSERT INTO hideout_upgrade (...) VALUES (...);
INSERT INTO post (...) VALUES (...);
INSERT INTO post_catalog_relation (...) VALUES (...);
```

- [ ] **Step 2: Keep content community-oriented**

Use concise Chinese descriptions focused on route planning, task preparation, loadouts, market value, boss risk, hideout progression, and team-up scenarios. Do not turn pages into long encyclopedia text.

- [ ] **Step 3: Document the expanded scope**

Update `docs/tarkov-data-sources.md` with the new target counts and the rule that phase-3 content is seeded from `database/seed-content-phase3.sql`.

### Task 2: Import And Verify Database Counts

**Files:**
- Read: `database/seed-content-phase3.sql`

- [ ] **Step 1: Import the phase-3 SQL**

Run the SQL against local `tarkov_community` using the same local MySQL instance used by the backend.

- [ ] **Step 2: Verify table counts**

Expected minimum counts after import:

```text
tarkov_quest >= 60
tarkov_item >= 60
tarkov_weapon >= 30
tarkov_ammo >= 35
map_extract >= 70
map_loot_area >= 55
hideout_station >= 12
hideout_upgrade >= 35
post >= 35
post_catalog_relation >= 45
```

### Task 3: Rendered Frontend Verification

**Files:**
- Read: `frontend/src/views/CatalogCenterView.vue`
- Read: `frontend/src/views/HomeView.vue`

- [ ] **Step 1: Build frontend**

Run `npm run build` in `frontend`.

- [ ] **Step 2: Browser-check catalog tabs**

Open these routes and verify that cards render, images are not broken, and there is no framework overlay:

```text
http://127.0.0.1:5173/catalog?tab=quests
http://127.0.0.1:5173/catalog?tab=items
http://127.0.0.1:5173/catalog?tab=weapons
http://127.0.0.1:5173/catalog?tab=ammo
http://127.0.0.1:5173/posts
```

- [ ] **Step 3: Commit and push**

```bash
git add database/seed-content-phase3.sql docs/tarkov-data-sources.md
git commit -m "feat: expand tarkov demo content"
git push
```
