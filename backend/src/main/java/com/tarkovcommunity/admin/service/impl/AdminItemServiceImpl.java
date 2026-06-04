package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminItemResponse;
import com.tarkovcommunity.admin.dto.AdminItemUpdateRequest;
import com.tarkovcommunity.admin.service.AdminItemService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.TarkovItem;
import com.tarkovcommunity.tarkov.mapper.TarkovItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminItemServiceImpl implements AdminItemService {

    private static final int MAX_PAGE_SIZE = 50;

    private final TarkovItemMapper itemMapper;

    @Override
    public PageResponse<AdminItemResponse> listItems(
            String itemType,
            String status,
            Boolean questNeeded,
            Boolean hideoutNeeded,
            Boolean keepSuggestion,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<TarkovItem> query = new LambdaQueryWrapper<TarkovItem>()
                .orderByAsc(TarkovItem::getId);

        if (StringUtils.hasText(itemType)) {
            query.eq(TarkovItem::getItemType, itemType);
        }

        if (StringUtils.hasText(status)) {
            query.eq(TarkovItem::getStatus, status);
        }

        if (questNeeded != null) {
            query.eq(TarkovItem::getQuestNeeded, questNeeded);
        }

        if (hideoutNeeded != null) {
            query.eq(TarkovItem::getHideoutNeeded, hideoutNeeded);
        }

        if (keepSuggestion != null) {
            query.eq(TarkovItem::getKeepSuggestion, keepSuggestion);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(TarkovItem::getNameEn, keyword)
                    .or()
                    .like(TarkovItem::getNameZh, keyword)
                    .or()
                    .like(TarkovItem::getItemType, keyword)
                    .or()
                    .like(TarkovItem::getRarity, keyword)
                    .or()
                    .like(TarkovItem::getGridSize, keyword)
                    .or()
                    .like(TarkovItem::getDescription, keyword));
        }

        Page<TarkovItem> itemPage = itemMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, itemPage.getTotal(), toResponses(itemPage.getRecords()));
    }

    @Override
    public AdminItemResponse updateItem(Long id, AdminItemUpdateRequest request) {
        TarkovItem item = itemMapper.selectById(id);
        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "物品不存在");
        }

        item.setNameEn(request.nameEn().trim());
        item.setNameZh(normalizeNullable(request.nameZh()));
        item.setItemType(normalizeNullable(request.itemType()));
        item.setRarity(normalizeNullable(request.rarity()));
        item.setGridSize(normalizeNullable(request.gridSize()));
        item.setQuestNeeded(request.questNeeded());
        item.setHideoutNeeded(request.hideoutNeeded());
        item.setKeepSuggestion(request.keepSuggestion());
        item.setDescription(normalizeNullable(request.description()));
        item.setStatus(request.status());
        itemMapper.updateById(item);

        return toResponse(itemMapper.selectById(id));
    }

    private static List<AdminItemResponse> toResponses(List<TarkovItem> items) {
        return items.stream()
                .map(AdminItemServiceImpl::toResponse)
                .toList();
    }

    private static AdminItemResponse toResponse(TarkovItem item) {
        return new AdminItemResponse(
                item.getId(),
                item.getNameEn(),
                item.getNameZh(),
                item.getItemType(),
                item.getRarity(),
                item.getGridSize(),
                item.getQuestNeeded(),
                item.getHideoutNeeded(),
                item.getKeepSuggestion(),
                item.getDescription(),
                item.getStatus()
        );
    }

    private static String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
