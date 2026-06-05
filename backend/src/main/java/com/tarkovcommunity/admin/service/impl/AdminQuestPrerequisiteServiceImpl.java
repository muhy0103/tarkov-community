package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminQuestPrerequisiteResponse;
import com.tarkovcommunity.admin.dto.AdminQuestPrerequisiteUpdateRequest;
import com.tarkovcommunity.admin.service.AdminQuestPrerequisiteService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.QuestPrerequisite;
import com.tarkovcommunity.tarkov.entity.TarkovQuest;
import com.tarkovcommunity.tarkov.mapper.QuestPrerequisiteMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovQuestMapper;
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
public class AdminQuestPrerequisiteServiceImpl implements AdminQuestPrerequisiteService {

    private static final int MAX_PAGE_SIZE = 50;

    private final QuestPrerequisiteMapper questPrerequisiteMapper;
    private final TarkovQuestMapper questMapper;

    @Override
    public PageResponse<AdminQuestPrerequisiteResponse> listQuestPrerequisites(
            Long questId,
            Long prerequisiteQuestId,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<QuestPrerequisite> query = new LambdaQueryWrapper<QuestPrerequisite>()
                .orderByAsc(QuestPrerequisite::getQuestId)
                .orderByAsc(QuestPrerequisite::getPrerequisiteQuestId)
                .orderByAsc(QuestPrerequisite::getId);

        if (questId != null) {
            query.eq(QuestPrerequisite::getQuestId, questId);
        }

        if (prerequisiteQuestId != null) {
            query.eq(QuestPrerequisite::getPrerequisiteQuestId, prerequisiteQuestId);
        }

        if (StringUtils.hasText(keyword)) {
            List<Long> matchingQuestIds = findQuestIdsByKeyword(keyword);
            if (matchingQuestIds.isEmpty()) {
                return PageResponse.of(safePage, safeSize, 0, List.of());
            }
            query.and(wrapper -> wrapper
                    .in(QuestPrerequisite::getQuestId, matchingQuestIds)
                    .or()
                    .in(QuestPrerequisite::getPrerequisiteQuestId, matchingQuestIds));
        }

        Page<QuestPrerequisite> prerequisitePage =
                questPrerequisiteMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, prerequisitePage.getTotal(), toResponses(prerequisitePage.getRecords()));
    }

    @Override
    public AdminQuestPrerequisiteResponse updateQuestPrerequisite(Long id, AdminQuestPrerequisiteUpdateRequest request) {
        QuestPrerequisite prerequisite = questPrerequisiteMapper.selectById(id);
        if (prerequisite == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "任务前置关系不存在");
        }

        if (Objects.equals(request.questId(), request.prerequisiteQuestId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "任务不能设置为自己的前置任务");
        }

        if (questMapper.selectById(request.questId()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "任务不存在");
        }

        if (questMapper.selectById(request.prerequisiteQuestId()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "前置任务不存在");
        }

        prerequisite.setQuestId(request.questId());
        prerequisite.setPrerequisiteQuestId(request.prerequisiteQuestId());
        questPrerequisiteMapper.updateById(prerequisite);

        return toResponses(List.of(questPrerequisiteMapper.selectById(id))).get(0);
    }

    private List<Long> findQuestIdsByKeyword(String keyword) {
        QueryWrapper<TarkovQuest> query = new QueryWrapper<TarkovQuest>()
                .select("id")
                .and(wrapper -> wrapper
                        .like("name_en", keyword)
                        .or()
                        .like("name_zh", keyword)
                        .or()
                        .like("quest_type", keyword));
        return questMapper.selectList(query).stream()
                .map(TarkovQuest::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<AdminQuestPrerequisiteResponse> toResponses(List<QuestPrerequisite> prerequisites) {
        Map<Long, TarkovQuest> quests = selectByIds(
                prerequisites.stream()
                        .flatMap(prerequisite -> List.of(
                                prerequisite.getQuestId(),
                                prerequisite.getPrerequisiteQuestId()
                        ).stream())
                        .filter(Objects::nonNull)
                        .toList(),
                questMapper::selectBatchIds,
                TarkovQuest::getId
        );

        return prerequisites.stream()
                .map(prerequisite -> toResponse(
                        prerequisite,
                        quests.get(prerequisite.getQuestId()),
                        quests.get(prerequisite.getPrerequisiteQuestId())
                ))
                .toList();
    }

    private static AdminQuestPrerequisiteResponse toResponse(
            QuestPrerequisite prerequisite,
            TarkovQuest quest,
            TarkovQuest prerequisiteQuest
    ) {
        return new AdminQuestPrerequisiteResponse(
                prerequisite.getId(),
                prerequisite.getQuestId(),
                displayName(quest),
                prerequisite.getPrerequisiteQuestId(),
                displayName(prerequisiteQuest)
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

    private static String displayName(TarkovQuest quest) {
        if (quest == null) {
            return null;
        }
        if (StringUtils.hasText(quest.getNameZh()) && StringUtils.hasText(quest.getNameEn())) {
            return quest.getNameZh() + " / " + quest.getNameEn();
        }
        if (StringUtils.hasText(quest.getNameZh())) {
            return quest.getNameZh();
        }
        return quest.getNameEn();
    }
}
