package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminQuestResponse;
import com.tarkovcommunity.admin.dto.AdminQuestUpdateRequest;
import com.tarkovcommunity.admin.service.AdminQuestService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.entity.TarkovQuest;
import com.tarkovcommunity.tarkov.entity.TarkovTrader;
import com.tarkovcommunity.tarkov.mapper.TarkovMapMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovQuestMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovTraderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminQuestServiceImpl implements AdminQuestService {

    private static final int MAX_PAGE_SIZE = 50;

    private final TarkovQuestMapper questMapper;
    private final TarkovTraderMapper traderMapper;
    private final TarkovMapMapper mapMapper;

    @Override
    public PageResponse<AdminQuestResponse> listQuests(Long traderId, Long mapId, String status, String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<TarkovQuest> query = new LambdaQueryWrapper<TarkovQuest>()
                .orderByAsc(TarkovQuest::getId);

        if (traderId != null) {
            query.eq(TarkovQuest::getTraderId, traderId);
        }

        if (mapId != null) {
            query.eq(TarkovQuest::getMapId, mapId);
        }

        if (StringUtils.hasText(status)) {
            query.eq(TarkovQuest::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(TarkovQuest::getNameEn, keyword)
                    .or()
                    .like(TarkovQuest::getNameZh, keyword)
                    .or()
                    .like(TarkovQuest::getQuestType, keyword)
                    .or()
                    .like(TarkovQuest::getDescription, keyword)
                    .or()
                    .like(TarkovQuest::getRewards, keyword)
                    .or()
                    .like(TarkovQuest::getUnlocks, keyword));
        }

        Page<TarkovQuest> questPage = questMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, questPage.getTotal(), toResponses(questPage.getRecords()));
    }

    @Override
    public AdminQuestResponse updateQuest(Long id, AdminQuestUpdateRequest request) {
        TarkovQuest quest = questMapper.selectById(id);
        if (quest == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "任务不存在");
        }

        TarkovTrader trader = traderMapper.selectById(request.traderId());
        if (trader == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "所属商人不存在");
        }

        if (request.mapId() != null && mapMapper.selectById(request.mapId()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "所属地图不存在");
        }

        quest.setTraderId(request.traderId());
        quest.setNameEn(request.nameEn().trim());
        quest.setNameZh(normalizeNullable(request.nameZh()));
        quest.setQuestType(normalizeNullable(request.questType()));
        quest.setMapId(request.mapId());
        quest.setDescription(normalizeNullable(request.description()));
        quest.setRewards(normalizeNullable(request.rewards()));
        quest.setUnlocks(normalizeNullable(request.unlocks()));
        quest.setStatus(request.status());
        questMapper.updateById(quest);

        return toResponses(List.of(questMapper.selectById(id))).get(0);
    }

    private List<AdminQuestResponse> toResponses(List<TarkovQuest> quests) {
        Map<Long, TarkovTrader> traders = selectByIds(
                quests.stream().map(TarkovQuest::getTraderId).filter(Objects::nonNull).toList(),
                traderMapper::selectBatchIds,
                TarkovTrader::getId
        );
        Map<Long, TarkovMap> maps = selectByIds(
                quests.stream().map(TarkovQuest::getMapId).filter(Objects::nonNull).toList(),
                mapMapper::selectBatchIds,
                TarkovMap::getId
        );

        return quests.stream()
                .map(quest -> toResponse(quest, traders.get(quest.getTraderId()), maps.get(quest.getMapId())))
                .toList();
    }

    private static AdminQuestResponse toResponse(TarkovQuest quest, TarkovTrader trader, TarkovMap map) {
        return new AdminQuestResponse(
                quest.getId(),
                quest.getTraderId(),
                trader == null ? null : trader.getNameEn(),
                quest.getNameEn(),
                quest.getNameZh(),
                quest.getQuestType(),
                quest.getMapId(),
                displayName(map == null ? null : map.getNameZh(), map == null ? null : map.getNameEn()),
                quest.getDescription(),
                quest.getRewards(),
                quest.getUnlocks(),
                quest.getStatus()
        );
    }

    private static <T> Map<Long, T> selectByIds(
            List<Long> ids,
            Function<List<Long>, List<T>> selector,
            Function<T, Long> idGetter
    ) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return selector.apply(ids.stream().distinct().toList())
                .stream()
                .collect(Collectors.toMap(idGetter, Function.identity()));
    }

    private static String displayName(String nameZh, String nameEn) {
        if (StringUtils.hasText(nameZh) && StringUtils.hasText(nameEn)) {
            return nameZh + " / " + nameEn;
        }
        if (StringUtils.hasText(nameZh)) {
            return nameZh;
        }
        return nameEn;
    }

    private static String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
