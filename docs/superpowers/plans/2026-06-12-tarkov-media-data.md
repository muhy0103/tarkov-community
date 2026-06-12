# Tarkov Media Data Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add image fields, API responses, detail-page rendering, and the first batch of Tarkov media-backed data.

**Architecture:** Store external media URLs in MySQL instead of copying images into the repo. Backend DTOs expose media fields from existing MyBatis entities, and the Vue detail page renders one clean visual block per catalog item. Seed data uses tarkov.dev asset links and keeps Wiki/source links documented.

**Tech Stack:** Spring Boot, MyBatis-Plus, MySQL, Vue 3 JavaScript, Element Plus.

---

### Task 1: Backend Contract

**Files:**
- Modify: `backend/src/test/java/com/tarkovcommunity/tarkov/TarkovCatalogMediaContractTests.java`
- Modify: `backend/src/main/java/com/tarkovcommunity/tarkov/dto/*Response.java`
- Modify: `backend/src/main/java/com/tarkovcommunity/tarkov/entity/TarkovMap.java`
- Modify: `backend/src/main/java/com/tarkovcommunity/tarkov/entity/TarkovWeapon.java`
- Modify: `backend/src/main/java/com/tarkovcommunity/tarkov/entity/TarkovAmmo.java`
- Modify: `backend/src/main/java/com/tarkovcommunity/tarkov/entity/Boss.java`
- Modify: `backend/src/main/java/com/tarkovcommunity/tarkov/service/impl/TarkovCatalogServiceImpl.java`

- [ ] Write failing tests for media fields in catalog list/detail responses.
- [ ] Add entity fields and DTO record components.
- [ ] Map media fields in catalog service list/detail methods.
- [ ] Run targeted backend tests and then the backend suite.

### Task 2: Database And Seed Data

**Files:**
- Modify: `database/schema.sql`
- Modify: `database/seed.sql`
- Modify: `docs/tarkov-data-sources.md`

- [ ] Add `image_url` columns to map, weapon, ammo, and boss tables.
- [ ] Update seed statements with first-batch image links for maps, traders, bosses, weapons, and ammo.
- [ ] Document that media links are stored as source URLs and should be refreshed from tarkov.dev/Wiki when needed.

### Task 3: Frontend Detail Display

**Files:**
- Modify: `frontend/src/views/CatalogDetailView.vue`

- [ ] Render a compact media panel when a detail object has `imageUrl` or `avatar`.
- [ ] Add fallback behavior for missing images.
- [ ] Keep the detail layout bright, clear, and community-oriented.
- [ ] Run frontend build and browser-check representative detail pages.

### Task 4: Publish

**Files:**
- Commit all intended files except local-only `database/admin-account.sql`.

- [ ] Run backend tests, frontend build, and git diff checks.
- [ ] Commit with a focused message.
- [ ] Push to GitHub.
