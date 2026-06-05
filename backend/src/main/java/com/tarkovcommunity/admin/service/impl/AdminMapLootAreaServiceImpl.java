package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminMapLootAreaResponse;
import com.tarkovcommunity.admin.dto.AdminMapLootAreaUpdateRequest;
import com.tarkovcommunity.admin.service.AdminMapLootAreaService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.MapLootArea;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.mapper.MapLootAreaMapper;
import com.tarkovcommunity.tarkov.mapper.TarkovMapMapper;
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
public class AdminMapLootAreaServiceImpl implements AdminMapLootAreaService {

    private static final int MAX_PAGE_SIZE = 50;

    private final MapLootAreaMapper mapLootAreaMapper;
    private final TarkovMapMapper mapMapper;

    @Override
    public PageResponse<AdminMapLootAreaResponse> listLootAreas(
            Long mapId,
            String riskLevel,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<MapLootArea> query = new LambdaQueryWrapper<MapLootArea>()
                .orderByAsc(MapLootArea::getMapId)
                .orderByAsc(MapLootArea::getRiskLevel)
                .orderByAsc(MapLootArea::getName)
                .orderByAsc(MapLootArea::getId);

        if (mapId != null) {
            query.eq(MapLootArea::getMapId, mapId);
        }

        if (StringUtils.hasText(riskLevel)) {
            query.eq(MapLootArea::getRiskLevel, riskLevel);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(MapLootArea::getName, keyword)
                    .or()
                    .like(MapLootArea::getLootType, keyword)
                    .or()
                    .like(MapLootArea::getRiskLevel, keyword)
                    .or()
                    .like(MapLootArea::getDescription, keyword));
        }

        Page<MapLootArea> lootAreaPage = mapLootAreaMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, lootAreaPage.getTotal(), toResponses(lootAreaPage.getRecords()));
    }

    @Override
    public AdminMapLootAreaResponse updateLootArea(Long id, AdminMapLootAreaUpdateRequest request) {
        MapLootArea lootArea = mapLootAreaMapper.selectById(id);
        if (lootArea == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Map loot area does not exist");
        }

        TarkovMap map = mapMapper.selectById(request.mapId());
        if (map == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Map does not exist");
        }

        lootArea.setMapId(request.mapId());
        lootArea.setName(request.name().trim());
        lootArea.setLootType(normalizeNullable(request.lootType()));
        lootArea.setRiskLevel(normalizeNullable(request.riskLevel()));
        lootArea.setDescription(normalizeNullable(request.description()));
        mapLootAreaMapper.updateById(lootArea);

        return toResponses(List.of(mapLootAreaMapper.selectById(id))).get(0);
    }

    private List<AdminMapLootAreaResponse> toResponses(List<MapLootArea> lootAreas) {
        Map<Long, TarkovMap> maps = selectByIds(
                lootAreas.stream().map(MapLootArea::getMapId).filter(Objects::nonNull).toList(),
                mapMapper::selectBatchIds,
                TarkovMap::getId
        );

        return lootAreas.stream()
                .map(lootArea -> toResponse(lootArea, maps.get(lootArea.getMapId())))
                .toList();
    }

    private static AdminMapLootAreaResponse toResponse(MapLootArea lootArea, TarkovMap map) {
        return new AdminMapLootAreaResponse(
                lootArea.getId(),
                lootArea.getMapId(),
                displayName(map == null ? null : map.getNameZh(), map == null ? null : map.getNameEn()),
                lootArea.getName(),
                lootArea.getLootType(),
                lootArea.getRiskLevel(),
                lootArea.getDescription()
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
