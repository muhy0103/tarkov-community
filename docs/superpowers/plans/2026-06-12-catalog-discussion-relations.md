# Catalog Discussion Relations Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a multi-to-multi relation between forum posts and Tarkov catalog data so posts can reference maps, traders, quests, items, weapons, ammo, bosses, and hideout stations, and catalog detail pages can surface related discussions.

**Architecture:** Add a dedicated `post_catalog_relation` table and a focused `PostCatalogRelationService` that validates catalog references, persists relation rows, and returns catalog summary DTOs. Keep `ForumPostServiceImpl` responsible for post workflows while delegating relation validation, replacement, filtering, and response hydration to the relation service.

**Tech Stack:** Spring Boot, MyBatis-Plus, MySQL, JUnit 5, Mockito, Vue 3 JavaScript, Element Plus, Vite.

**Execution Status (2026-06-14):** Tasks 1-8 have been implemented on `feature/catalog-discussion-relations` and pushed. Final verification covered backend tests, frontend build, catalog-to-discussion browser flow, post creation with catalog relation, mobile overflow checks, git status, and a focused sensitive-value scan.

---

## File Structure

### Backend

- Modify: `database/schema.sql`
  Add `post_catalog_relation` table after `post` so subsequent comment/like/favorite tables remain semantically grouped under forum data.
- Create: `backend/src/main/java/com/tarkovcommunity/forum/entity/PostCatalogRelation.java`
  MyBatis-Plus entity for relation rows.
- Create: `backend/src/main/java/com/tarkovcommunity/forum/mapper/PostCatalogRelationMapper.java`
  Mapper for relation CRUD and catalog-filtered post IDs.
- Create: `backend/src/main/java/com/tarkovcommunity/forum/dto/PostCatalogRelationRequest.java`
  Request item used by create/update post APIs.
- Create: `backend/src/main/java/com/tarkovcommunity/forum/dto/RelatedCatalogResponse.java`
  Compact catalog tag returned with post summaries and details.
- Create: `backend/src/main/java/com/tarkovcommunity/forum/service/PostCatalogRelationService.java`
  Service contract used by `ForumPostServiceImpl`.
- Create: `backend/src/main/java/com/tarkovcommunity/forum/service/impl/PostCatalogRelationServiceImpl.java`
  Validation, save/replace, batch hydration, and relation-filter helpers.
- Modify: `backend/src/main/java/com/tarkovcommunity/forum/dto/PostCreateRequest.java`
  Add `relations`.
- Modify: `backend/src/main/java/com/tarkovcommunity/forum/dto/PostUpdateRequest.java`
  Add `relations`.
- Modify: `backend/src/main/java/com/tarkovcommunity/forum/dto/PostSummaryResponse.java`
  Add `relations`.
- Modify: `backend/src/main/java/com/tarkovcommunity/forum/dto/PostDetailResponse.java`
  Add `relations`.
- Modify: `backend/src/main/java/com/tarkovcommunity/forum/service/ForumPostService.java`
  Add `catalogType` and `catalogId` filters to `listPosts`.
- Modify: `backend/src/main/java/com/tarkovcommunity/forum/controller/ForumPostController.java`
  Accept relation filter query params.
- Modify: `backend/src/main/java/com/tarkovcommunity/forum/service/impl/ForumPostServiceImpl.java`
  Delegate relation persistence and response hydration.
- Test: `backend/src/test/java/com/tarkovcommunity/forum/PostCatalogRelationServiceImplTests.java`
- Test: `backend/src/test/java/com/tarkovcommunity/forum/ForumPostServiceImplTests.java`
- Test: `backend/src/test/java/com/tarkovcommunity/forum/ForumPostControllerTests.java`

### Frontend

- Modify: `frontend/src/api/catalogApi.js`
  Add `catalogType` mapping helpers and a unified `fetchCatalogCollections`.
- Modify: `frontend/src/api/postApi.js`
  Keep `fetchPosts` generic and allow relation query params already passed through.
- Create: `frontend/src/views/CatalogCenterView.vue`
  New `/catalog` page.
- Modify: `frontend/src/router/index.js`
  Add `/catalog` route.
- Modify: `frontend/src/App.vue`
  Add nav entry for catalog center if the app header uses a static nav list.
- Modify: `frontend/src/views/PostCreateView.vue`
  Add relation selector, include relations in drafts and submit payload.
- Modify: `frontend/src/views/PostBoardView.vue`
  Read relation filters from route query and show relation chips on cards.
- Modify: `frontend/src/views/PostDetailView.vue`
  Show full related catalog section.
- Modify: `frontend/src/views/CatalogDetailView.vue`
  Show related discussions for current catalog item.
- Modify: `frontend/src/views/HomeView.vue`
  Link “查看资料” or catalog preview heading to `/catalog` where useful.
- Modify: `frontend/src/style.css`
  Add shared styles for catalog center, relation selector, relation tags, and related discussion list.

---

### Task 1: Database, Entity, Mapper, And Relation Service Contract

**Files:**
- Modify: `database/schema.sql`
- Create: `backend/src/main/java/com/tarkovcommunity/forum/entity/PostCatalogRelation.java`
- Create: `backend/src/main/java/com/tarkovcommunity/forum/mapper/PostCatalogRelationMapper.java`
- Create: `backend/src/main/java/com/tarkovcommunity/forum/dto/PostCatalogRelationRequest.java`
- Create: `backend/src/main/java/com/tarkovcommunity/forum/dto/RelatedCatalogResponse.java`
- Create: `backend/src/main/java/com/tarkovcommunity/forum/service/PostCatalogRelationService.java`
- Create: `backend/src/test/java/com/tarkovcommunity/forum/PostCatalogRelationServiceImplTests.java`

- [ ] **Step 1: Add the failing service contract tests**

Create `backend/src/test/java/com/tarkovcommunity/forum/PostCatalogRelationServiceImplTests.java` with tests for deduplication and max relation count. The implementation class will not exist yet, so this test fails at compile time first.

```java
@ExtendWith(MockitoExtension.class)
class PostCatalogRelationServiceImplTests {

    @Mock private PostCatalogRelationMapper relationMapper;
    @Mock private TarkovMapMapper mapMapper;
    @Mock private TarkovTraderMapper traderMapper;
    @Mock private TarkovQuestMapper questMapper;
    @Mock private TarkovItemMapper itemMapper;
    @Mock private TarkovWeaponMapper weaponMapper;
    @Mock private TarkovAmmoMapper ammoMapper;
    @Mock private BossMapper bossMapper;
    @Mock private HideoutStationMapper hideoutStationMapper;

    @Test
    void replacesRelationsWithDedupedRows() {
        PostCatalogRelationServiceImpl service = service();
        TarkovMap map = new TarkovMap();
        map.setId(1L);
        map.setNameZh("海关");
        map.setNameEn("Customs");
        map.setImageUrl("https://assets.tarkov.dev/customs.webp");
        map.setStatus("ENABLED");
        given(mapMapper.selectById(1L)).willReturn(map);

        service.replaceRelations(10L, List.of(
                new PostCatalogRelationRequest("MAP", 1L, "主要地图"),
                new PostCatalogRelationRequest("MAP", 1L, "重复提交")
        ));

        verify(relationMapper).delete(any());
        ArgumentCaptor<PostCatalogRelation> captor = ArgumentCaptor.forClass(PostCatalogRelation.class);
        verify(relationMapper).insert(captor.capture());
        assertThat(captor.getValue().getPostId()).isEqualTo(10L);
        assertThat(captor.getValue().getCatalogType()).isEqualTo("MAP");
        assertThat(captor.getValue().getCatalogId()).isEqualTo(1L);
        assertThat(captor.getValue().getRelationNote()).isEqualTo("主要地图");
    }

    @Test
    void rejectsMoreThanSixRelations() {
        PostCatalogRelationServiceImpl service = service();
        List<PostCatalogRelationRequest> requests = LongStream.rangeClosed(1, 7)
                .mapToObj(id -> new PostCatalogRelationRequest("MAP", id, null))
                .toList();

        assertThatThrownBy(() -> service.replaceRelations(10L, requests))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("最多关联 6 个资料");
    }

    private PostCatalogRelationServiceImpl service() {
        return new PostCatalogRelationServiceImpl(
                relationMapper,
                mapMapper,
                traderMapper,
                questMapper,
                itemMapper,
                weaponMapper,
                ammoMapper,
                bossMapper,
                hideoutStationMapper
        );
    }
}
```

- [ ] **Step 2: Run the failing test**

Run:

```powershell
$env:JAVA_HOME='C:\Users\muhy\.jdks\openjdk-25.0.1'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\mvnw.cmd "-Dtest=PostCatalogRelationServiceImplTests" test
```

Expected: compilation fails because `PostCatalogRelationServiceImpl`, `PostCatalogRelation`, `PostCatalogRelationRequest`, and `RelatedCatalogResponse` do not exist.

- [ ] **Step 3: Add schema table**

Append the table directly after the `post` table definition in `database/schema.sql`:

```sql
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
```

- [ ] **Step 4: Add entity and mapper**

Create `backend/src/main/java/com/tarkovcommunity/forum/entity/PostCatalogRelation.java`:

```java
package com.tarkovcommunity.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post_catalog_relation")
public class PostCatalogRelation {
    private Long id;
    private Long postId;
    private String catalogType;
    private Long catalogId;
    private String relationNote;
    private LocalDateTime createdAt;
}
```

Create `backend/src/main/java/com/tarkovcommunity/forum/mapper/PostCatalogRelationMapper.java`:

```java
package com.tarkovcommunity.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.forum.entity.PostCatalogRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostCatalogRelationMapper extends BaseMapper<PostCatalogRelation> {

    @Select("""
            SELECT p.*
            FROM post p
            INNER JOIN post_catalog_relation r ON r.post_id = p.id
            WHERE r.catalog_type = #{catalogType}
              AND r.catalog_id = #{catalogId}
              AND p.status = 'NORMAL'
              AND p.deleted = 0
            ORDER BY p.pinned DESC, p.recommended DESC, p.created_at DESC
            """)
    Page<com.tarkovcommunity.forum.entity.Post> selectRelatedPostsPage(
            Page<com.tarkovcommunity.forum.entity.Post> page,
            @Param("catalogType") String catalogType,
            @Param("catalogId") Long catalogId
    );
}
```

- [ ] **Step 5: Add DTOs and service contract**

Create `backend/src/main/java/com/tarkovcommunity/forum/dto/PostCatalogRelationRequest.java`:

```java
package com.tarkovcommunity.forum.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PostCatalogRelationRequest(
        @NotNull(message = "资料类型不能为空")
        @Pattern(regexp = "MAP|TRADER|QUEST|ITEM|WEAPON|AMMO|BOSS|HIDEOUT", message = "资料类型不正确")
        String catalogType,

        @NotNull(message = "资料 ID 不能为空")
        Long catalogId,

        @Size(max = 120, message = "关联备注不能超过 120 个字符")
        String relationNote
) {
}
```

Create `backend/src/main/java/com/tarkovcommunity/forum/dto/RelatedCatalogResponse.java`:

```java
package com.tarkovcommunity.forum.dto;

public record RelatedCatalogResponse(
        String catalogType,
        Long catalogId,
        String name,
        String subtitle,
        String imageUrl,
        String routeKind,
        String relationNote
) {
}
```

Create `backend/src/main/java/com/tarkovcommunity/forum/service/PostCatalogRelationService.java`:

```java
package com.tarkovcommunity.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.forum.dto.PostCatalogRelationRequest;
import com.tarkovcommunity.forum.dto.RelatedCatalogResponse;
import com.tarkovcommunity.forum.entity.Post;

import java.util.List;
import java.util.Map;

public interface PostCatalogRelationService {
    void replaceRelations(Long postId, List<PostCatalogRelationRequest> requests);

    Map<Long, List<RelatedCatalogResponse>> findRelationsByPostIds(List<Long> postIds);

    Page<Post> selectRelatedPostsPage(String catalogType, Long catalogId, int page, int size);
}
```

- [ ] **Step 6: Commit the schema and contract**

Run:

```powershell
git add database/schema.sql backend/src/main/java/com/tarkovcommunity/forum/entity/PostCatalogRelation.java backend/src/main/java/com/tarkovcommunity/forum/mapper/PostCatalogRelationMapper.java backend/src/main/java/com/tarkovcommunity/forum/dto/PostCatalogRelationRequest.java backend/src/main/java/com/tarkovcommunity/forum/dto/RelatedCatalogResponse.java backend/src/main/java/com/tarkovcommunity/forum/service/PostCatalogRelationService.java backend/src/test/java/com/tarkovcommunity/forum/PostCatalogRelationServiceImplTests.java
git commit -m "feat: add post catalog relation contract"
```

---

### Task 2: Implement Relation Validation And Catalog Summary Hydration

**Files:**
- Create: `backend/src/main/java/com/tarkovcommunity/forum/service/impl/PostCatalogRelationServiceImpl.java`
- Modify: `backend/src/test/java/com/tarkovcommunity/forum/PostCatalogRelationServiceImplTests.java`

- [ ] **Step 1: Extend failing tests for invalid catalog and response hydration**

Add two tests to `PostCatalogRelationServiceImplTests`:

```java
@Test
void rejectsMissingCatalogRecord() {
    PostCatalogRelationServiceImpl service = service();
    given(weaponMapper.selectById(404L)).willReturn(null);

    assertThatThrownBy(() -> service.replaceRelations(10L, List.of(
            new PostCatalogRelationRequest("WEAPON", 404L, null)
    )))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("关联资料不存在或已停用");
}

@Test
void hydratesRelatedCatalogResponsesByPostId() {
    PostCatalogRelationServiceImpl service = service();
    PostCatalogRelation relation = new PostCatalogRelation();
    relation.setPostId(10L);
    relation.setCatalogType("WEAPON");
    relation.setCatalogId(1L);
    relation.setRelationNote("主武器");
    given(relationMapper.selectList(any())).willReturn(List.of(relation));
    TarkovWeapon weapon = new TarkovWeapon();
    weapon.setId(1L);
    weapon.setNameEn("AK-74N");
    weapon.setNameZh("AK-74N");
    weapon.setWeaponType("Assault rifle");
    weapon.setCaliber("5.45x39");
    weapon.setImageUrl("https://assets.tarkov.dev/ak-74n.webp");
    weapon.setStatus("ENABLED");
    given(weaponMapper.selectById(1L)).willReturn(weapon);

    Map<Long, List<RelatedCatalogResponse>> result = service.findRelationsByPostIds(List.of(10L));

    assertThat(result.get(10L)).singleElement().satisfies(item -> {
        assertThat(item.catalogType()).isEqualTo("WEAPON");
        assertThat(item.catalogId()).isEqualTo(1L);
        assertThat(item.name()).isEqualTo("AK-74N");
        assertThat(item.subtitle()).isEqualTo("Assault rifle · 5.45x39");
        assertThat(item.routeKind()).isEqualTo("weapons");
        assertThat(item.relationNote()).isEqualTo("主武器");
    });
}
```

- [ ] **Step 2: Run the relation service tests**

Run:

```powershell
$env:JAVA_HOME='C:\Users\muhy\.jdks\openjdk-25.0.1'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\mvnw.cmd "-Dtest=PostCatalogRelationServiceImplTests" test
```

Expected: tests fail because implementation is missing.

- [ ] **Step 3: Implement `PostCatalogRelationServiceImpl`**

Create `backend/src/main/java/com/tarkovcommunity/forum/service/impl/PostCatalogRelationServiceImpl.java` with these methods:

```java
@Service
@RequiredArgsConstructor
public class PostCatalogRelationServiceImpl implements PostCatalogRelationService {

    private static final int MAX_RELATIONS = 6;

    private final PostCatalogRelationMapper relationMapper;
    private final TarkovMapMapper mapMapper;
    private final TarkovTraderMapper traderMapper;
    private final TarkovQuestMapper questMapper;
    private final TarkovItemMapper itemMapper;
    private final TarkovWeaponMapper weaponMapper;
    private final TarkovAmmoMapper ammoMapper;
    private final BossMapper bossMapper;
    private final HideoutStationMapper hideoutStationMapper;

    @Override
    public void replaceRelations(Long postId, List<PostCatalogRelationRequest> requests) {
        relationMapper.delete(new LambdaQueryWrapper<PostCatalogRelation>()
                .eq(PostCatalogRelation::getPostId, postId));
        List<PostCatalogRelationRequest> normalized = normalizeRequests(requests);
        for (PostCatalogRelationRequest request : normalized) {
            CatalogSummary summary = requireCatalog(request.catalogType(), request.catalogId());
            PostCatalogRelation relation = new PostCatalogRelation();
            relation.setPostId(postId);
            relation.setCatalogType(summary.catalogType());
            relation.setCatalogId(summary.catalogId());
            relation.setRelationNote(normalizeNullable(request.relationNote()));
            relationMapper.insert(relation);
        }
    }

    @Override
    public Map<Long, List<RelatedCatalogResponse>> findRelationsByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<PostCatalogRelation> relations = relationMapper.selectList(new LambdaQueryWrapper<PostCatalogRelation>()
                .in(PostCatalogRelation::getPostId, postIds)
                .orderByAsc(PostCatalogRelation::getId));
        return relations.stream()
                .map(relation -> Map.entry(relation.getPostId(), toResponse(relation)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    @Override
    public Page<Post> selectRelatedPostsPage(String catalogType, Long catalogId, int page, int size) {
        CatalogSummary summary = requireCatalog(catalogType, catalogId);
        return relationMapper.selectRelatedPostsPage(new Page<>(page, size), summary.catalogType(), summary.catalogId());
    }
}
```

Also implement private helpers in the same class:

```java
private List<PostCatalogRelationRequest> normalizeRequests(List<PostCatalogRelationRequest> requests) {
    if (requests == null || requests.isEmpty()) {
        return List.of();
    }
    if (requests.size() > MAX_RELATIONS) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "最多关联 6 个资料");
    }
    Map<String, PostCatalogRelationRequest> deduped = new LinkedHashMap<>();
    for (PostCatalogRelationRequest request : requests) {
        String catalogType = normalizeCatalogType(request.catalogType());
        Long catalogId = request.catalogId();
        String key = catalogType + ":" + catalogId;
        deduped.putIfAbsent(key, new PostCatalogRelationRequest(catalogType, catalogId, request.relationNote()));
    }
    return List.copyOf(deduped.values());
}

private String normalizeCatalogType(String catalogType) {
    if (!StringUtils.hasText(catalogType)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "资料类型不能为空");
    }
    return catalogType.trim().toUpperCase(Locale.ROOT);
}
```

Use a private record:

```java
private record CatalogSummary(
        String catalogType,
        Long catalogId,
        String name,
        String subtitle,
        String imageUrl,
        String routeKind
) {
}
```

Implement `requireCatalog` with the full switch:

```java
private CatalogSummary requireCatalog(String catalogType, Long catalogId) {
    if (catalogId == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "catalogId is required");
    }
    return switch (normalizeCatalogType(catalogType)) {
        case "MAP" -> {
            TarkovMap map = mapMapper.selectById(catalogId);
            requireEnabled(map == null ? null : map.getStatus());
            yield new CatalogSummary("MAP", map.getId(), displayName(map.getNameZh(), map.getNameEn()), map.getDifficulty(), map.getImageUrl(), "maps");
        }
        case "TRADER" -> {
            TarkovTrader trader = traderMapper.selectById(catalogId);
            requireEnabled(trader == null ? null : trader.getStatus());
            yield new CatalogSummary("TRADER", trader.getId(), displayName(trader.getNameZh(), trader.getNameEn()), trader.getUnlockCondition(), trader.getAvatar(), "traders");
        }
        case "QUEST" -> {
            TarkovQuest quest = questMapper.selectById(catalogId);
            requireEnabled(quest == null ? null : quest.getStatus());
            yield new CatalogSummary("QUEST", quest.getId(), displayName(quest.getNameZh(), quest.getNameEn()), quest.getQuestType(), null, "quests");
        }
        case "ITEM" -> {
            TarkovItem item = itemMapper.selectById(catalogId);
            requireEnabled(item == null ? null : item.getStatus());
            yield new CatalogSummary("ITEM", item.getId(), displayName(item.getNameZh(), item.getNameEn()), compactText(item.getItemType(), item.getRarity()), null, "items");
        }
        case "WEAPON" -> {
            TarkovWeapon weapon = weaponMapper.selectById(catalogId);
            requireEnabled(weapon == null ? null : weapon.getStatus());
            yield new CatalogSummary("WEAPON", weapon.getId(), displayName(weapon.getNameZh(), weapon.getNameEn()), compactText(weapon.getWeaponType(), weapon.getCaliber()), weapon.getImageUrl(), "weapons");
        }
        case "AMMO" -> {
            TarkovAmmo ammo = ammoMapper.selectById(catalogId);
            requireEnabled(ammo == null ? null : ammo.getStatus());
            yield new CatalogSummary("AMMO", ammo.getId(), displayName(ammo.getNameZh(), ammo.getNameEn()), compactText(ammo.getCaliber(), "Pen " + ammo.getPenetration()), ammo.getImageUrl(), "ammo");
        }
        case "BOSS" -> {
            Boss boss = bossMapper.selectById(catalogId);
            requireEnabled(boss == null ? null : boss.getStatus());
            yield new CatalogSummary("BOSS", boss.getId(), displayName(boss.getNameZh(), boss.getNameEn()), boss.getEquipmentSummary(), boss.getImageUrl(), "bosses");
        }
        case "HIDEOUT" -> {
            HideoutStation station = hideoutStationMapper.selectById(catalogId);
            requireEnabled(station == null ? null : station.getStatus());
            yield new CatalogSummary("HIDEOUT", station.getId(), displayName(station.getNameZh(), station.getNameEn()), "Hideout station", null, "hideout");
        }
        default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported catalog type");
    };
}
```

Add the remaining private helpers in the same class:

```java
private RelatedCatalogResponse toResponse(PostCatalogRelation relation) {
    CatalogSummary summary = requireCatalog(relation.getCatalogType(), relation.getCatalogId());
    return new RelatedCatalogResponse(
            summary.catalogType(),
            summary.catalogId(),
            summary.name(),
            summary.subtitle(),
            summary.imageUrl(),
            summary.routeKind(),
            relation.getRelationNote()
    );
}

private void requireEnabled(String status) {
    if (!"ENABLED".equalsIgnoreCase(status)) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Catalog data does not exist or is disabled");
    }
}

private String displayName(String nameZh, String nameEn) {
    return StringUtils.hasText(nameZh) ? nameZh : nameEn;
}

private String compactText(String first, String second) {
    return Stream.of(first, second)
            .filter(StringUtils::hasText)
            .collect(Collectors.joining(" · "));
}

private String normalizeNullable(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
}
```

- [ ] **Step 4: Run relation service tests**

Run:

```powershell
$env:JAVA_HOME='C:\Users\muhy\.jdks\openjdk-25.0.1'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\mvnw.cmd "-Dtest=PostCatalogRelationServiceImplTests" test
```

Expected: tests pass.

- [ ] **Step 5: Commit relation implementation**

Run:

```powershell
git add backend/src/main/java/com/tarkovcommunity/forum/service/impl/PostCatalogRelationServiceImpl.java backend/src/test/java/com/tarkovcommunity/forum/PostCatalogRelationServiceImplTests.java
git commit -m "feat: implement catalog relation service"
```

---

### Task 3: Wire Relations Into Post Create, Update, Detail, And List Filtering

**Files:**
- Modify: `backend/src/main/java/com/tarkovcommunity/forum/dto/PostCreateRequest.java`
- Modify: `backend/src/main/java/com/tarkovcommunity/forum/dto/PostUpdateRequest.java`
- Modify: `backend/src/main/java/com/tarkovcommunity/forum/dto/PostSummaryResponse.java`
- Modify: `backend/src/main/java/com/tarkovcommunity/forum/dto/PostDetailResponse.java`
- Modify: `backend/src/main/java/com/tarkovcommunity/forum/service/ForumPostService.java`
- Modify: `backend/src/main/java/com/tarkovcommunity/forum/controller/ForumPostController.java`
- Modify: `backend/src/main/java/com/tarkovcommunity/forum/service/impl/ForumPostServiceImpl.java`
- Modify: `backend/src/test/java/com/tarkovcommunity/forum/ForumPostServiceImplTests.java`
- Modify: `backend/src/test/java/com/tarkovcommunity/forum/ForumPostControllerTests.java`

- [ ] **Step 1: Write failing service tests**

Add tests to `ForumPostServiceImplTests`:

```java
@Mock private PostCatalogRelationService postCatalogRelationService;

@Test
void createPostSavesCatalogRelations() {
    ForumPostServiceImpl service = service();
    given(categoryMapper.selectById(1L)).willReturn(enabledCategory());
    SysUser author = user();
    PostCreateRequest request = new PostCreateRequest(
            author.getId(),
            1L,
            "Customs route",
            "This is a complete Customs route guide.",
            "ROUTE",
            null,
            List.of(new PostCatalogRelationRequest("MAP", 1L, "主要地图"))
    );

    service.createPost(request, author);

    ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
    verify(postMapper).insert(postCaptor.capture());
    verify(postCatalogRelationService).replaceRelations(eq(postCaptor.getValue().getId()), eq(request.relations()));
}

@Test
void listPostsCanFilterByCatalogRelation() {
    ForumPostServiceImpl service = service();
    Page<Post> relatedPage = new Page<>(1, 10, 1);
    relatedPage.setRecords(List.of(post()));
    given(postCatalogRelationService.selectRelatedPostsPage("MAP", 1L, 1, 10)).willReturn(relatedPage);
    given(postCatalogRelationService.findRelationsByPostIds(List.of(1L))).willReturn(Map.of(1L, relationResponses()));

    PageResponse<PostSummaryResponse> response = service.listPosts(null, null, null, null, "LATEST", "MAP", 1L, 1, 10);

    assertThat(response.records()).singleElement()
            .satisfies(post -> assertThat(post.relations()).hasSize(1));
}
```

- [ ] **Step 2: Run failing forum tests**

Run:

```powershell
$env:JAVA_HOME='C:\Users\muhy\.jdks\openjdk-25.0.1'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\mvnw.cmd "-Dtest=ForumPostServiceImplTests,ForumPostControllerTests" test
```

Expected: compile failures because DTO constructors and service method signatures do not include relation fields.

- [ ] **Step 3: Extend DTOs**

Modify `PostCreateRequest` and `PostUpdateRequest` by adding:

```java
@Size(max = 6, message = "最多关联 6 个资料")
List<PostCatalogRelationRequest> relations
```

The final `PostCreateRequest` constructor order is:

```java
Long userId,
Long categoryId,
String title,
String content,
String postType,
String coverImage,
List<PostCatalogRelationRequest> relations
```

The final `PostUpdateRequest` constructor order is:

```java
Long categoryId,
String title,
String content,
String postType,
String coverImage,
List<PostCatalogRelationRequest> relations
```

Modify `PostSummaryResponse` and `PostDetailResponse` to append:

```java
List<RelatedCatalogResponse> relations
```

- [ ] **Step 4: Extend post service and controller signatures**

Modify `ForumPostService.listPosts`:

```java
PageResponse<PostSummaryResponse> listPosts(
        String categoryCode,
        String keyword,
        String postType,
        Boolean recommended,
        String sort,
        String catalogType,
        Long catalogId,
        int page,
        int size
);
```

Modify `ForumPostController.listPosts` to accept:

```java
@RequestParam(required = false) String catalogType,
@RequestParam(required = false) Long catalogId,
```

Pass both values into `forumPostService.listPosts(...)`.

- [ ] **Step 5: Wire `ForumPostServiceImpl`**

Inject:

```java
private final PostCatalogRelationService postCatalogRelationService;
```

In `createPost`, after `postMapper.insert(post)`:

```java
postCatalogRelationService.replaceRelations(post.getId(), request.relations());
```

In `updatePost`, after updating post fields and before returning:

```java
postCatalogRelationService.replaceRelations(post.getId(), request.relations());
```

In `listPosts`, branch when relation filter is present:

```java
Page<Post> postPage;
if (StringUtils.hasText(catalogType) && catalogId != null) {
    postPage = postCatalogRelationService.selectRelatedPostsPage(catalogType, catalogId, safePage, safeSize);
} else {
    applySort(query, sort);
    postPage = postMapper.selectPage(new Page<>(safePage, safeSize), query);
}
```

In `toSummaries`, add relation map:

```java
Map<Long, List<RelatedCatalogResponse>> relations = postCatalogRelationService.findRelationsByPostIds(
        posts.stream().map(Post::getId).filter(Objects::nonNull).toList()
);
```

Append `relations.getOrDefault(post.getId(), List.of())` to `PostSummaryResponse`.

In `getPost`, load relations:

```java
List<RelatedCatalogResponse> relations = postCatalogRelationService
        .findRelationsByPostIds(List.of(post.getId()))
        .getOrDefault(post.getId(), List.of());
```

Append `relations` to `PostDetailResponse`.

- [ ] **Step 6: Update controller tests**

In `ForumPostControllerTests`, update JSON expectations:

```java
.andExpect(jsonPath("$.data.records[0].relations[0].catalogType").value("MAP"))
.andExpect(jsonPath("$.data.records[0].relations[0].name").value("海关"))
```

Add a list request with relation filters:

```java
mockMvc.perform(get("/api/posts")
        .param("catalogType", "MAP")
        .param("catalogId", "1"))
        .andExpect(status().isOk());
verify(forumPostService).listPosts(null, null, null, null, "LATEST", "MAP", 1L, 1, 10);
```

- [ ] **Step 7: Run targeted backend tests**

Run:

```powershell
$env:JAVA_HOME='C:\Users\muhy\.jdks\openjdk-25.0.1'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\mvnw.cmd "-Dtest=ForumPostServiceImplTests,ForumPostControllerTests,PostCatalogRelationServiceImplTests" test
```

Expected: all targeted tests pass.

- [ ] **Step 8: Commit post API relation wiring**

Run:

```powershell
git add backend/src/main/java/com/tarkovcommunity/forum backend/src/test/java/com/tarkovcommunity/forum
git commit -m "feat: link posts with catalog relations"
```

---

### Task 4: Catalog Center Page And Route

**Files:**
- Create: `frontend/src/views/CatalogCenterView.vue`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/App.vue`
- Modify: `frontend/src/api/catalogApi.js`
- Modify: `frontend/src/style.css`

- [ ] **Step 1: Add catalog collection helpers**

Modify `frontend/src/api/catalogApi.js`:

```js
export const catalogTypeOptions = [
  { label: '地图', type: 'MAP', kind: 'maps', fetcher: fetchMaps },
  { label: '商人', type: 'TRADER', kind: 'traders', fetcher: fetchTraders },
  { label: '任务', type: 'QUEST', kind: 'quests', fetcher: fetchQuests },
  { label: '物品', type: 'ITEM', kind: 'items', fetcher: fetchItems },
  { label: '武器', type: 'WEAPON', kind: 'weapons', fetcher: fetchWeapons },
  { label: '弹药', type: 'AMMO', kind: 'ammo', fetcher: fetchAmmo },
  { label: 'Boss', type: 'BOSS', kind: 'bosses', fetcher: fetchBosses },
  { label: '藏身处', type: 'HIDEOUT', kind: 'hideout', fetcher: fetchHideoutStations },
]

export function routeKindForCatalogType(type) {
  return catalogTypeOptions.find((option) => option.type === type)?.kind || ''
}

export async function fetchCatalogCollections() {
  const entries = await Promise.all(catalogTypeOptions.map(async (option) => ({
    ...option,
    items: await option.fetcher(),
  })))
  return entries
}
```

- [ ] **Step 2: Create catalog center page**

Create `frontend/src/views/CatalogCenterView.vue` with:

```vue
<script setup>
import { computed, onMounted, ref } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { fetchCatalogCollections } from '../api/catalogApi'

const loading = ref(false)
const errorMessage = ref('')
const activeKind = ref('maps')
const keyword = ref('')
const collections = ref([])

const activeCollection = computed(() => collections.value.find((item) => item.kind === activeKind.value))
const filteredItems = computed(() => {
  const normalized = keyword.value.trim().toLowerCase()
  const items = activeCollection.value?.items || []
  if (!normalized) return items
  return items.filter((item) => [item.nameZh, item.nameEn, item.name, item.description, item.caliber, item.weaponType]
    .filter(Boolean)
    .some((value) => String(value).toLowerCase().includes(normalized)))
})

function itemName(item) {
  return item.nameZh || item.nameEn || item.name || '未命名资料'
}

function itemMeta(item) {
  return [item.nameEn, item.difficulty, item.weaponType, item.caliber, item.unlockCondition]
    .filter(Boolean)
    .join(' · ')
}

function itemMedia(item) {
  return item.imageUrl || item.avatar || ''
}

async function loadCatalogCenter() {
  loading.value = true
  errorMessage.value = ''
  try {
    collections.value = await fetchCatalogCollections()
  } catch (error) {
    errorMessage.value = error?.response?.data?.message || error?.message || '资料中心暂时无法加载'
  } finally {
    loading.value = false
  }
}

onMounted(loadCatalogCenter)
</script>
```

Template structure:

```vue
<template>
  <div class="catalog-center-view">
    <section class="board-header">
      <div>
        <h2>资料中心</h2>
        <p>集中浏览地图、商人、Boss、武器、弹药、任务和藏身处资料，并进入相关讨论。</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadCatalogCenter">刷新资料</el-button>
    </section>

    <section class="board-toolbar">
      <el-input v-model="keyword" :prefix-icon="Search" placeholder="搜索资料名称、口径、类型或说明" clearable />
      <el-tabs v-model="activeKind" class="catalog-center-tabs">
        <el-tab-pane v-for="collection in collections" :key="collection.kind" :name="collection.kind" :label="collection.label" />
      </el-tabs>
    </section>

    <el-alert v-if="errorMessage" :title="errorMessage" type="warning" show-icon class="home-alert" />
    <el-skeleton v-if="loading" :rows="8" animated class="home-skeleton" />

    <section v-else class="catalog-center-grid">
      <RouterLink
        v-for="item in filteredItems"
        :key="`${activeCollection.kind}-${item.id}`"
        class="catalog-center-card"
        :to="{ name: 'catalog-detail', params: { kind: activeCollection.kind, id: item.id } }"
      >
        <span class="catalog-center-media">
          <img v-if="itemMedia(item)" :src="itemMedia(item)" :alt="itemName(item)" loading="lazy" />
        </span>
        <span class="catalog-center-copy">
          <strong>{{ itemName(item) }}</strong>
          <small>{{ itemMeta(item) || activeCollection.label }}</small>
          <p>{{ item.description || item.equipmentSummary || '暂无说明' }}</p>
        </span>
      </RouterLink>
    </section>
  </div>
</template>
```

- [ ] **Step 3: Add route and nav link**

In `frontend/src/router/index.js`:

```js
import CatalogCenterView from '../views/CatalogCenterView.vue'
```

Add route before detail route:

```js
{
  path: '/catalog',
  name: 'catalog-center',
  component: CatalogCenterView,
  meta: {
    title: '资料中心',
  },
},
```

If `frontend/src/App.vue` has a static nav list, add:

```js
{ label: '资料中心', to: '/catalog' },
```

- [ ] **Step 4: Add styles**

Add to `frontend/src/style.css`:

```css
.catalog-center-view {
  display: grid;
  gap: 18px;
}

.catalog-center-tabs {
  min-width: 0;
}

.catalog-center-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.catalog-center-card {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  gap: 12px;
  min-height: 118px;
  padding: 14px;
  color: inherit;
  text-decoration: none;
  border: 1px solid #dbe7e6;
  border-radius: 8px;
  background: #ffffff;
}

.catalog-center-media {
  display: grid;
  overflow: hidden;
  height: 88px;
  place-items: center;
  border-radius: 8px;
  background: #ecfeff;
}

.catalog-center-media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.catalog-center-copy {
  display: grid;
  min-width: 0;
  gap: 6px;
}

.catalog-center-copy strong {
  overflow: hidden;
  color: #111827;
  font-size: 15px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.catalog-center-copy small {
  color: #667483;
  font-size: 12px;
}

.catalog-center-copy p {
  display: -webkit-box;
  overflow: hidden;
  margin: 0;
  color: #52616d;
  font-size: 13px;
  line-height: 1.55;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
```

Add mobile rule:

```css
@media (max-width: 760px) {
  .catalog-center-grid {
    grid-template-columns: 1fr;
  }
}
```

- [ ] **Step 5: Run frontend build**

Run:

```powershell
npm run build
```

Expected: build succeeds with only the existing Rolldown annotation and chunk warnings.

- [ ] **Step 6: Commit catalog center**

Run:

```powershell
git add frontend/src/api/catalogApi.js frontend/src/views/CatalogCenterView.vue frontend/src/router/index.js frontend/src/App.vue frontend/src/style.css
git commit -m "feat: add catalog center page"
```

---

### Task 5: Post Create Relation Selector

**Files:**
- Modify: `frontend/src/views/PostCreateView.vue`
- Modify: `frontend/src/style.css`

- [ ] **Step 1: Load catalog options in post create page**

In `PostCreateView.vue`, change imports:

```js
import { catalogTypeOptions, fetchCatalogCollections } from '../api/catalogApi'
```

Add state:

```js
const catalogCollections = ref([])
const relationPicker = ref({
  catalogType: 'MAP',
  catalogId: null,
})
```

Extend `form`:

```js
relations: [],
```

Update `loadCategories` to load catalog collections:

```js
const [categoryData, catalogData, postData] = await Promise.all([
  fetchCategories(),
  fetchCatalogCollections(),
  isEditMode.value ? fetchPostDetail(editPostId.value) : Promise.resolve(null),
])
catalogCollections.value = catalogData
```

- [ ] **Step 2: Add relation helper functions**

Add:

```js
const activeRelationCollection = computed(() => (
  catalogCollections.value.find((item) => item.type === relationPicker.value.catalogType)
))

const selectedRelationIds = computed(() => new Set(
  form.value.relations
    .filter((item) => item.catalogType === relationPicker.value.catalogType)
    .map((item) => item.catalogId)
))

function relationName(item) {
  return item.nameZh || item.nameEn || item.name || '未命名资料'
}

function relationSubtitle(item) {
  return [item.nameEn, item.weaponType, item.caliber, item.difficulty, item.unlockCondition]
    .filter(Boolean)
    .join(' · ')
}

function relationRouteKind(type) {
  return catalogTypeOptions.find((option) => option.type === type)?.kind || ''
}

function addRelation() {
  const collection = activeRelationCollection.value
  const item = collection?.items.find((entry) => entry.id === relationPicker.value.catalogId)
  if (!item || form.value.relations.length >= 6) {
    return
  }
  if (form.value.relations.some((relation) => relation.catalogType === collection.type && relation.catalogId === item.id)) {
    return
  }
  form.value.relations.push({
    catalogType: collection.type,
    catalogId: item.id,
    relationNote: '',
    name: relationName(item),
    subtitle: relationSubtitle(item),
    imageUrl: item.imageUrl || item.avatar || '',
    routeKind: collection.kind,
  })
  relationPicker.value.catalogId = null
}

function removeRelation(index) {
  form.value.relations.splice(index, 1)
}
```

- [ ] **Step 3: Persist relations in drafts and payload**

When restoring a draft:

```js
relations: Array.isArray(draft.relations) ? draft.relations : [],
```

When saving a draft:

```js
relations: form.value.relations,
```

When loading an edit post:

```js
relations: postData.relations || [],
```

When submitting:

```js
relations: form.value.relations.map((relation) => ({
  catalogType: relation.catalogType,
  catalogId: relation.catalogId,
  relationNote: relation.relationNote || null,
})),
```

- [ ] **Step 4: Add selector UI**

Add this after the post type select in the template:

```vue
<div class="post-relation-panel">
  <div class="relation-picker-row">
    <el-select v-model="relationPicker.catalogType" placeholder="资料类型">
      <el-option
        v-for="option in catalogTypeOptions"
        :key="option.type"
        :label="option.label"
        :value="option.type"
      />
    </el-select>
    <el-select
      v-model="relationPicker.catalogId"
      placeholder="选择关联资料"
      filterable
      clearable
    >
      <el-option
        v-for="item in activeRelationCollection?.items || []"
        :key="item.id"
        :label="relationName(item)"
        :value="item.id"
        :disabled="selectedRelationIds.has(item.id)"
      >
        <span>{{ relationName(item) }}</span>
        <small>{{ relationSubtitle(item) }}</small>
      </el-option>
    </el-select>
    <el-button :disabled="!relationPicker.catalogId || form.relations.length >= 6" @click="addRelation">
      添加
    </el-button>
  </div>

  <div v-if="form.relations.length" class="relation-chip-list">
    <span v-for="(relation, index) in form.relations" :key="`${relation.catalogType}-${relation.catalogId}`" class="relation-chip">
      <img v-if="relation.imageUrl" :src="relation.imageUrl" :alt="relation.name" />
      <span>{{ relation.name }}</span>
      <button type="button" @click="removeRelation(index)">×</button>
    </span>
  </div>
</div>
```

- [ ] **Step 5: Add relation selector styles**

Add to `frontend/src/style.css`:

```css
.post-relation-panel {
  display: grid;
  gap: 10px;
  padding: 14px;
  border: 1px solid #dbe7e6;
  border-radius: 8px;
  background: #f8fbfb;
}

.relation-picker-row {
  display: grid;
  grid-template-columns: 150px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
}

.relation-chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.relation-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  max-width: 100%;
  padding: 5px 8px;
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
  border: 1px solid #ccfbf1;
  border-radius: 8px;
  background: #ffffff;
}

.relation-chip img {
  width: 22px;
  height: 22px;
  object-fit: cover;
  border-radius: 6px;
}

.relation-chip button {
  border: 0;
  color: #667483;
  background: transparent;
  cursor: pointer;
}
```

- [ ] **Step 6: Run frontend build**

Run:

```powershell
npm run build
```

Expected: build succeeds.

- [ ] **Step 7: Commit relation selector**

Run:

```powershell
git add frontend/src/views/PostCreateView.vue frontend/src/style.css
git commit -m "feat: add post catalog relation selector"
```

---

### Task 6: Relation Tags On Post Board And Post Detail

**Files:**
- Modify: `frontend/src/views/PostBoardView.vue`
- Modify: `frontend/src/views/PostDetailView.vue`
- Modify: `frontend/src/style.css`

- [ ] **Step 1: Add frontend relation route helper**

In both views, add:

```js
function catalogRoute(relation) {
  return {
    name: 'catalog-detail',
    params: {
      kind: relation.routeKind,
      id: relation.catalogId,
    },
  }
}
```

- [ ] **Step 2: Render relation chips on post cards**

In `PostBoardView.vue`, inside each post card after metadata:

```vue
<div v-if="post.relations?.length" class="post-relation-tags">
  <RouterLink
    v-for="relation in post.relations.slice(0, 3)"
    :key="`${post.id}-${relation.catalogType}-${relation.catalogId}`"
    class="post-relation-tag"
    :to="catalogRoute(relation)"
  >
    {{ relation.name }}
  </RouterLink>
  <span v-if="post.relations.length > 3" class="post-relation-more">
    +{{ post.relations.length - 3 }}
  </span>
</div>
```

- [ ] **Step 3: Render full relation list on post detail**

In `PostDetailView.vue`, above the content body:

```vue
<section v-if="post.relations?.length" class="post-related-catalogs">
  <h3>关联资料</h3>
  <div class="post-related-grid">
    <RouterLink
      v-for="relation in post.relations"
      :key="`${relation.catalogType}-${relation.catalogId}`"
      class="post-related-card"
      :to="catalogRoute(relation)"
    >
      <img v-if="relation.imageUrl" :src="relation.imageUrl" :alt="relation.name" loading="lazy" />
      <span>
        <strong>{{ relation.name }}</strong>
        <small>{{ relation.subtitle || relation.catalogType }}</small>
      </span>
    </RouterLink>
  </div>
</section>
```

- [ ] **Step 4: Add styles**

Add:

```css
.post-relation-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 8px 0;
}

.post-relation-tag,
.post-relation-more {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 3px 8px;
  color: #0f766e;
  font-size: 12px;
  font-weight: 700;
  text-decoration: none;
  border: 1px solid #ccfbf1;
  border-radius: 8px;
  background: #f0fdfa;
}

.post-related-catalogs {
  display: grid;
  gap: 10px;
  margin: 18px 0;
}

.post-related-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.post-related-card {
  display: grid;
  grid-template-columns: 46px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  padding: 10px;
  color: inherit;
  text-decoration: none;
  border: 1px solid #dbe7e6;
  border-radius: 8px;
  background: #ffffff;
}

.post-related-card img {
  width: 46px;
  height: 38px;
  object-fit: cover;
  border-radius: 8px;
}

.post-related-card strong {
  display: block;
  overflow: hidden;
  color: #111827;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-related-card small {
  color: #667483;
  font-size: 12px;
}
```

- [ ] **Step 5: Run frontend build**

Run:

```powershell
npm run build
```

Expected: build succeeds.

- [ ] **Step 6: Commit post relation display**

Run:

```powershell
git add frontend/src/views/PostBoardView.vue frontend/src/views/PostDetailView.vue frontend/src/style.css
git commit -m "feat: show catalog relations on posts"
```

---

### Task 7: Related Discussions On Catalog Detail And Board Relation Filtering

**Files:**
- Modify: `frontend/src/views/CatalogDetailView.vue`
- Modify: `frontend/src/views/PostBoardView.vue`
- Modify: `frontend/src/style.css`

- [ ] **Step 1: Map catalog route kind to catalog type**

In `CatalogDetailView.vue`, add:

```js
const catalogTypeByKind = {
  maps: 'MAP',
  traders: 'TRADER',
  quests: 'QUEST',
  items: 'ITEM',
  weapons: 'WEAPON',
  ammo: 'AMMO',
  bosses: 'BOSS',
  hideout: 'HIDEOUT',
}
```

Add state:

```js
const relatedPosts = ref({
  page: 1,
  size: 5,
  total: 0,
  pages: 0,
  records: [],
})
const relatedPostsLoading = ref(false)
```

- [ ] **Step 2: Load related posts after detail loads**

Import:

```js
import { fetchPosts } from '../api/postApi'
```

Add:

```js
async function loadRelatedPosts() {
  const catalogType = catalogTypeByKind[route.params.kind]
  if (!catalogType || !route.params.id) {
    relatedPosts.value = { page: 1, size: 5, total: 0, pages: 0, records: [] }
    return
  }
  relatedPostsLoading.value = true
  try {
    relatedPosts.value = await fetchPosts({
      page: 1,
      size: 5,
      catalogType,
      catalogId: Number(route.params.id),
      sort: 'LATEST',
    })
  } finally {
    relatedPostsLoading.value = false
  }
}
```

Call it after `detail.value = await fetchCatalogDetail(...)`:

```js
await loadRelatedPosts()
```

- [ ] **Step 3: Render related discussions**

Add below relation sections in `CatalogDetailView.vue`:

```vue
<section class="detail-related-discussions">
  <div class="section-heading compact-heading">
    <h3>相关讨论</h3>
    <RouterLink
      class="inline-action-link"
      :to="{ name: 'post-board', query: { catalogType: catalogTypeByKind[route.params.kind], catalogId: route.params.id } }"
    >
      查看更多
    </RouterLink>
  </div>

  <el-skeleton v-if="relatedPostsLoading" :rows="3" animated />
  <div v-else-if="relatedPosts.records.length" class="related-discussion-list">
    <RouterLink
      v-for="post in relatedPosts.records"
      :key="post.id"
      class="related-discussion-item"
      :to="{ name: 'post-detail', params: { id: post.id } }"
    >
      <strong>{{ post.title }}</strong>
      <span>{{ post.summary }}</span>
      <small>浏览 {{ post.viewCount }} · 评论 {{ post.commentCount }}</small>
    </RouterLink>
  </div>
  <div v-else class="post-empty compact-empty">
    <div>
      <h4>暂无相关讨论</h4>
      <p>可以发布一条帖子，把这份资料和你的路线、配装或问题关联起来。</p>
    </div>
  </div>
</section>
```

- [ ] **Step 4: Support relation query filter banner on post board**

In `PostBoardView.vue`, add filters:

```js
catalogType: '',
catalogId: '',
```

Add to `queryParams`:

```js
catalogType: filters.value.catalogType || undefined,
catalogId: filters.value.catalogId || undefined,
```

Update `applyRouteQuery`:

```js
catalogType: stringQueryValue(route.query.catalogType),
catalogId: stringQueryValue(route.query.catalogId),
```

Add a clear helper:

```js
function clearCatalogFilter() {
  filters.value.catalogType = ''
  filters.value.catalogId = ''
  router.replace({
    name: 'post-board',
    query: {
      ...route.query,
      catalogType: undefined,
      catalogId: undefined,
    },
  })
}
```

Add banner above toolbar:

```vue
<el-alert
  v-if="filters.catalogType && filters.catalogId"
  title="当前正在查看指定资料的相关讨论"
  type="success"
  show-icon
  class="home-alert"
>
  <template #default>
    <el-button size="small" @click="clearCatalogFilter">清除资料筛选</el-button>
  </template>
</el-alert>
```

- [ ] **Step 5: Add related discussion styles**

Add:

```css
.detail-related-discussions {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.compact-heading {
  align-items: center;
}

.related-discussion-list {
  display: grid;
  gap: 10px;
}

.related-discussion-item {
  display: grid;
  gap: 5px;
  padding: 12px;
  color: inherit;
  text-decoration: none;
  border: 1px solid #dbe7e6;
  border-radius: 8px;
  background: #ffffff;
}

.related-discussion-item strong {
  overflow: hidden;
  color: #111827;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-discussion-item span {
  display: -webkit-box;
  overflow: hidden;
  color: #52616d;
  font-size: 13px;
  line-height: 1.55;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.related-discussion-item small {
  color: #7b8794;
  font-size: 12px;
}
```

- [ ] **Step 6: Run frontend build**

Run:

```powershell
npm run build
```

Expected: build succeeds.

- [ ] **Step 7: Commit related discussions**

Run:

```powershell
git add frontend/src/views/CatalogDetailView.vue frontend/src/views/PostBoardView.vue frontend/src/style.css
git commit -m "feat: show related discussions on catalog details"
```

---

### Task 8: End-To-End Verification And Final Push

**Files:**
- Verify all files changed in Tasks 1-7.

- [ ] **Step 1: Run full backend tests**

Run:

```powershell
$env:JAVA_HOME='C:\Users\muhy\.jdks\openjdk-25.0.1'; $env:Path="$env:JAVA_HOME\bin;$env:Path"; .\mvnw.cmd test
```

Expected: all backend tests pass.

- [ ] **Step 2: Run frontend build**

Run:

```powershell
npm run build
```

Expected: build succeeds with only existing Vite/Rolldown warnings.

- [ ] **Step 3: Browser verify user flows**

Use the in-app browser and verify:

1. `/catalog` loads and shows catalog cards.
2. `/catalog/maps/1` shows related discussions section.
3. `/posts?catalogType=MAP&catalogId=1` loads without horizontal overflow.
4. A logged-in user can open `/posts/new`, add a relation, and submit a post.
5. The new post detail shows the relation card.

- [ ] **Step 4: Check git status and sensitive-value scan**

Run:

```powershell
git status --short
git diff --cached --check
git diff --cached | rg -n -f .git/info/local-sensitive-patterns.txt
```

Expected:

1. `database/admin-account.sql` may remain uncommitted and must not be staged.
2. No whitespace errors.
3. Sensitive-value scan has no matches.

- [ ] **Step 5: Push all committed task branches or main commits**

Run:

```powershell
git push origin main
```

Expected: remote `main` updates successfully.

---

## Plan Self-Review

### Spec Coverage

- Multi-to-multi table: Task 1.
- Service-layer catalog validation: Task 2.
- Create/edit post relation persistence: Task 3.
- Post list relation filtering: Task 3 and Task 7.
- Post list/detail relation display: Task 6.
- Catalog center page: Task 4.
- Catalog detail related discussions: Task 7.
- Testing and browser verification: Task 8.

### Scope Control

This plan keeps the existing `post` table foreign-key columns untouched and does not migrate old data. It adds the new relation table and uses it for the new user-facing flow.

### Type Consistency

The plan consistently uses:

- Request type: `PostCatalogRelationRequest`
- Response type: `RelatedCatalogResponse`
- Service: `PostCatalogRelationService`
- Entity: `PostCatalogRelation`
- Query params: `catalogType`, `catalogId`
- Catalog type enum strings: `MAP`, `TRADER`, `QUEST`, `ITEM`, `WEAPON`, `AMMO`, `BOSS`, `HIDEOUT`
