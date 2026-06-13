package com.tarkovcommunity.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.forum.dto.PostCatalogRelationRequest;
import com.tarkovcommunity.forum.dto.RelatedCatalogResponse;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostCatalogRelation;
import com.tarkovcommunity.forum.mapper.PostCatalogRelationMapper;
import com.tarkovcommunity.forum.service.PostCatalogRelationService;
import com.tarkovcommunity.tarkov.entity.Boss;
import com.tarkovcommunity.tarkov.entity.HideoutStation;
import com.tarkovcommunity.tarkov.entity.TarkovAmmo;
import com.tarkovcommunity.tarkov.entity.TarkovItem;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.entity.TarkovQuest;
import com.tarkovcommunity.tarkov.entity.TarkovTrader;
import com.tarkovcommunity.tarkov.entity.TarkovWeapon;
import com.tarkovcommunity.tarkov.mapper.BossMapper;
import com.tarkovcommunity.tarkov.mapper.HideoutStationMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovAmmoMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovItemMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovMapMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovQuestMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovTraderMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovWeaponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCatalogRelationServiceImpl implements PostCatalogRelationService {

    private static final int MAX_RELATIONS = 6;
    private static final int MAX_PAGE_SIZE = 50;
    private static final String ENABLED = "ENABLED";
    private static final String CATALOG_NOT_FOUND_MESSAGE = "关联资料不存在或已停用";

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
    @Transactional
    public void replaceRelations(Long postId, List<PostCatalogRelationRequest> requests) {
        List<NormalizedRelationRequest> normalizedRequests = normalizeRequests(requests);
        normalizedRequests.forEach(request -> requireCatalog(request.catalogType(), request.catalogId()));

        relationMapper.delete(new LambdaQueryWrapper<PostCatalogRelation>()
                .eq(PostCatalogRelation::getPostId, postId));

        for (NormalizedRelationRequest request : normalizedRequests) {
            PostCatalogRelation relation = new PostCatalogRelation();
            relation.setPostId(postId);
            relation.setCatalogType(request.catalogType());
            relation.setCatalogId(request.catalogId());
            relation.setRelationNote(request.relationNote());
            relationMapper.insert(relation);
        }
    }

    @Override
    public Map<Long, List<RelatedCatalogResponse>> findRelationsByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> normalizedPostIds = postIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (normalizedPostIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<PostCatalogRelation> relations = relationMapper.selectList(new LambdaQueryWrapper<PostCatalogRelation>()
                .in(PostCatalogRelation::getPostId, normalizedPostIds)
                .orderByAsc(PostCatalogRelation::getId));
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Map<Long, CatalogSummary>> catalogSummaries = loadCatalogSummaries(relations);
        Map<Long, List<RelatedCatalogResponse>> groupedRelations = new LinkedHashMap<>();
        for (PostCatalogRelation relation : relations) {
            if (relation == null || relation.getPostId() == null) {
                continue;
            }
            groupedRelations.computeIfAbsent(relation.getPostId(), ignored -> new ArrayList<>())
                    .add(toResponse(relation, catalogSummaries));
        }
        return groupedRelations;
    }

    @Override
    public Page<Post> selectRelatedPostsPage(String catalogType, Long catalogId, int page, int size) {
        CatalogSummary catalog = requireCatalog(catalogType, catalogId);
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        return relationMapper.selectRelatedPostsPage(new Page<>(safePage, safeSize), catalog.catalogType(), catalog.catalogId());
    }

    private List<NormalizedRelationRequest> normalizeRequests(List<PostCatalogRelationRequest> requests) {
        List<PostCatalogRelationRequest> safeRequests = requests == null ? List.of() : requests;
        if (safeRequests.size() > MAX_RELATIONS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "最多关联 6 个资料");
        }

        Map<String, NormalizedRelationRequest> uniqueRequests = new LinkedHashMap<>();
        for (PostCatalogRelationRequest request : safeRequests) {
            if (request == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "关联资料不能为空");
            }
            String catalogType = normalizeCatalogType(request.catalogType());
            if (request.catalogId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "资料 ID 不能为空");
            }
            String key = catalogType + ":" + request.catalogId();
            uniqueRequests.putIfAbsent(key, new NormalizedRelationRequest(
                    catalogType,
                    request.catalogId(),
                    normalizeNullable(request.relationNote())
            ));
        }
        return new ArrayList<>(uniqueRequests.values());
    }

    private String normalizeCatalogType(String catalogType) {
        if (!StringUtils.hasText(catalogType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "资料类型不能为空");
        }
        return catalogType.trim().toUpperCase(Locale.ROOT);
    }

    private CatalogSummary requireCatalog(String catalogType, Long catalogId) {
        String normalizedType = normalizeCatalogType(catalogType);
        if (catalogId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "资料 ID 不能为空");
        }

        return switch (normalizedType) {
            case "MAP" -> {
                TarkovMap map = requireCatalogRecord(mapMapper.selectById(catalogId), TarkovMap::getStatus);
                yield new CatalogSummary(
                        normalizedType,
                        map.getId(),
                        displayName(map.getNameZh(), map.getNameEn()),
                        normalizeNullable(map.getDifficulty()),
                        normalizeNullable(map.getImageUrl()),
                        "maps"
                );
            }
            case "TRADER" -> {
                TarkovTrader trader = requireCatalogRecord(traderMapper.selectById(catalogId), TarkovTrader::getStatus);
                yield new CatalogSummary(
                        normalizedType,
                        trader.getId(),
                        displayName(trader.getNameZh(), trader.getNameEn()),
                        normalizeNullable(trader.getUnlockCondition()),
                        normalizeNullable(trader.getAvatar()),
                        "traders"
                );
            }
            case "QUEST" -> {
                TarkovQuest quest = requireCatalogRecord(questMapper.selectById(catalogId), TarkovQuest::getStatus);
                yield new CatalogSummary(
                        normalizedType,
                        quest.getId(),
                        displayName(quest.getNameZh(), quest.getNameEn()),
                        normalizeNullable(quest.getQuestType()),
                        null,
                        "quests"
                );
            }
            case "ITEM" -> {
                TarkovItem item = requireCatalogRecord(itemMapper.selectById(catalogId), TarkovItem::getStatus);
                yield new CatalogSummary(
                        normalizedType,
                        item.getId(),
                        displayName(item.getNameZh(), item.getNameEn()),
                        compactText(item.getItemType(), item.getRarity()),
                        null,
                        "items"
                );
            }
            case "WEAPON" -> {
                TarkovWeapon weapon = requireCatalogRecord(weaponMapper.selectById(catalogId), TarkovWeapon::getStatus);
                yield new CatalogSummary(
                        normalizedType,
                        weapon.getId(),
                        displayName(weapon.getNameZh(), weapon.getNameEn()),
                        compactText(weapon.getWeaponType(), weapon.getCaliber()),
                        normalizeNullable(weapon.getImageUrl()),
                        "weapons"
                );
            }
            case "AMMO" -> {
                TarkovAmmo ammo = requireCatalogRecord(ammoMapper.selectById(catalogId), TarkovAmmo::getStatus);
                String penetration = ammo.getPenetration() == null ? null : "Pen " + ammo.getPenetration();
                yield new CatalogSummary(
                        normalizedType,
                        ammo.getId(),
                        displayName(ammo.getNameZh(), ammo.getNameEn()),
                        compactText(ammo.getCaliber(), penetration),
                        normalizeNullable(ammo.getImageUrl()),
                        "ammo"
                );
            }
            case "BOSS" -> {
                Boss boss = requireCatalogRecord(bossMapper.selectById(catalogId), Boss::getStatus);
                yield new CatalogSummary(
                        normalizedType,
                        boss.getId(),
                        displayName(boss.getNameZh(), boss.getNameEn()),
                        normalizeNullable(boss.getEquipmentSummary()),
                        normalizeNullable(boss.getImageUrl()),
                        "bosses"
                );
            }
            case "HIDEOUT" -> {
                HideoutStation station = requireCatalogRecord(hideoutStationMapper.selectById(catalogId), HideoutStation::getStatus);
                yield new CatalogSummary(
                        normalizedType,
                        station.getId(),
                        displayName(station.getNameZh(), station.getNameEn()),
                        "Hideout station",
                        null,
                        "hideout"
                );
            }
            default -> throw unsupportedCatalogType();
        };
    }

    private Map<String, Map<Long, CatalogSummary>> loadCatalogSummaries(List<PostCatalogRelation> relations) {
        Map<String, List<Long>> idsByType = new LinkedHashMap<>();
        for (PostCatalogRelation relation : relations) {
            if (relation == null || relation.getPostId() == null) {
                continue;
            }
            String catalogType = normalizeCatalogType(relation.getCatalogType());
            if (relation.getCatalogId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "资料 ID 不能为空");
            }
            if (!isSupportedCatalogType(catalogType)) {
                throw unsupportedCatalogType();
            }
            idsByType.computeIfAbsent(catalogType, ignored -> new ArrayList<>())
                    .add(relation.getCatalogId());
        }

        Map<String, Map<Long, CatalogSummary>> summariesByType = new LinkedHashMap<>();
        for (Map.Entry<String, List<Long>> entry : idsByType.entrySet()) {
            summariesByType.put(entry.getKey(), loadCatalogSummaries(entry.getKey(), entry.getValue()));
        }
        return summariesByType;
    }

    private Map<Long, CatalogSummary> loadCatalogSummaries(String catalogType, List<Long> catalogIds) {
        List<Long> distinctCatalogIds = catalogIds.stream().distinct().toList();
        return switch (catalogType) {
            case "MAP" -> toSummaryMap(
                    mapMapper.selectBatchIds(distinctCatalogIds),
                    TarkovMap::getStatus,
                    map -> new CatalogSummary(
                            catalogType,
                            map.getId(),
                            displayName(map.getNameZh(), map.getNameEn()),
                            normalizeNullable(map.getDifficulty()),
                            normalizeNullable(map.getImageUrl()),
                            "maps"
                    )
            );
            case "TRADER" -> toSummaryMap(
                    traderMapper.selectBatchIds(distinctCatalogIds),
                    TarkovTrader::getStatus,
                    trader -> new CatalogSummary(
                            catalogType,
                            trader.getId(),
                            displayName(trader.getNameZh(), trader.getNameEn()),
                            normalizeNullable(trader.getUnlockCondition()),
                            normalizeNullable(trader.getAvatar()),
                            "traders"
                    )
            );
            case "QUEST" -> toSummaryMap(
                    questMapper.selectBatchIds(distinctCatalogIds),
                    TarkovQuest::getStatus,
                    quest -> new CatalogSummary(
                            catalogType,
                            quest.getId(),
                            displayName(quest.getNameZh(), quest.getNameEn()),
                            normalizeNullable(quest.getQuestType()),
                            null,
                            "quests"
                    )
            );
            case "ITEM" -> toSummaryMap(
                    itemMapper.selectBatchIds(distinctCatalogIds),
                    TarkovItem::getStatus,
                    item -> new CatalogSummary(
                            catalogType,
                            item.getId(),
                            displayName(item.getNameZh(), item.getNameEn()),
                            compactText(item.getItemType(), item.getRarity()),
                            null,
                            "items"
                    )
            );
            case "WEAPON" -> toSummaryMap(
                    weaponMapper.selectBatchIds(distinctCatalogIds),
                    TarkovWeapon::getStatus,
                    weapon -> new CatalogSummary(
                            catalogType,
                            weapon.getId(),
                            displayName(weapon.getNameZh(), weapon.getNameEn()),
                            compactText(weapon.getWeaponType(), weapon.getCaliber()),
                            normalizeNullable(weapon.getImageUrl()),
                            "weapons"
                    )
            );
            case "AMMO" -> toSummaryMap(
                    ammoMapper.selectBatchIds(distinctCatalogIds),
                    TarkovAmmo::getStatus,
                    ammo -> {
                        String penetration = ammo.getPenetration() == null ? null : "Pen " + ammo.getPenetration();
                        return new CatalogSummary(
                                catalogType,
                                ammo.getId(),
                                displayName(ammo.getNameZh(), ammo.getNameEn()),
                                compactText(ammo.getCaliber(), penetration),
                                normalizeNullable(ammo.getImageUrl()),
                                "ammo"
                        );
                    }
            );
            case "BOSS" -> toSummaryMap(
                    bossMapper.selectBatchIds(distinctCatalogIds),
                    Boss::getStatus,
                    boss -> new CatalogSummary(
                            catalogType,
                            boss.getId(),
                            displayName(boss.getNameZh(), boss.getNameEn()),
                            normalizeNullable(boss.getEquipmentSummary()),
                            normalizeNullable(boss.getImageUrl()),
                            "bosses"
                    )
            );
            case "HIDEOUT" -> toSummaryMap(
                    hideoutStationMapper.selectBatchIds(distinctCatalogIds),
                    HideoutStation::getStatus,
                    station -> new CatalogSummary(
                            catalogType,
                            station.getId(),
                            displayName(station.getNameZh(), station.getNameEn()),
                            "Hideout station",
                            null,
                            "hideout"
                    )
            );
            default -> throw unsupportedCatalogType();
        };
    }

    private static <T> Map<Long, CatalogSummary> toSummaryMap(
            List<T> records,
            Function<T, String> statusGetter,
            Function<T, CatalogSummary> summaryMapper
    ) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyMap();
        }
        return records.stream()
                .map(record -> summaryMapper.apply(requireCatalogRecord(record, statusGetter)))
                .collect(Collectors.toMap(
                        CatalogSummary::catalogId,
                        Function.identity(),
                        (existing, ignored) -> existing
                ));
    }

    private RelatedCatalogResponse toResponse(
            PostCatalogRelation relation,
            Map<String, Map<Long, CatalogSummary>> catalogSummaries
    ) {
        String catalogType = normalizeCatalogType(relation.getCatalogType());
        if (relation.getCatalogId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "资料 ID 不能为空");
        }
        if (!isSupportedCatalogType(catalogType)) {
            throw unsupportedCatalogType();
        }

        CatalogSummary summary = catalogSummaries
                .getOrDefault(catalogType, Collections.emptyMap())
                .get(relation.getCatalogId());
        if (summary == null) {
            throw catalogNotFound();
        }
        return new RelatedCatalogResponse(
                summary.catalogType(),
                summary.catalogId(),
                summary.name(),
                summary.subtitle(),
                summary.imageUrl(),
                summary.routeKind(),
                normalizeNullable(relation.getRelationNote())
        );
    }

    private static <T> T requireCatalogRecord(T record, Function<T, String> statusGetter) {
        if (record == null) {
            throw catalogNotFound();
        }
        requireEnabled(statusGetter.apply(record));
        return record;
    }

    private static boolean isSupportedCatalogType(String catalogType) {
        return switch (catalogType) {
            case "MAP", "TRADER", "QUEST", "ITEM", "WEAPON", "AMMO", "BOSS", "HIDEOUT" -> true;
            default -> false;
        };
    }

    private static void requireEnabled(String status) {
        if (!ENABLED.equals(status)) {
            throw catalogNotFound();
        }
    }

    private static String displayName(String nameZh, String nameEn) {
        String normalizedZh = normalizeNullable(nameZh);
        if (normalizedZh != null) {
            return normalizedZh;
        }
        String normalizedEn = normalizeNullable(nameEn);
        return normalizedEn == null ? "未命名资料" : normalizedEn;
    }

    private static String compactText(String first, String second) {
        String normalizedFirst = normalizeNullable(first);
        String normalizedSecond = normalizeNullable(second);
        if (normalizedFirst == null) {
            return normalizedSecond;
        }
        if (normalizedSecond == null) {
            return normalizedFirst;
        }
        return normalizedFirst + " · " + normalizedSecond;
    }

    private static String normalizeNullable(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private static ResponseStatusException catalogNotFound() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, CATALOG_NOT_FOUND_MESSAGE);
    }

    private static ResponseStatusException unsupportedCatalogType() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "不支持的资料类型");
    }

    private record NormalizedRelationRequest(String catalogType, Long catalogId, String relationNote) {
    }

    private record CatalogSummary(
            String catalogType,
            Long catalogId,
            String name,
            String subtitle,
            String imageUrl,
            String routeKind
    ) {
    }
}
