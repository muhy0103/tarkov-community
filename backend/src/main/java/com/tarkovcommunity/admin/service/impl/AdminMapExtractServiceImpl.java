package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminMapExtractResponse;
import com.tarkovcommunity.admin.dto.AdminMapExtractUpdateRequest;
import com.tarkovcommunity.admin.service.AdminMapExtractService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.MapExtract;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.mapper.MapExtractMapper;
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
public class AdminMapExtractServiceImpl implements AdminMapExtractService {

    private static final int MAX_PAGE_SIZE = 50;

    private final MapExtractMapper mapExtractMapper;
    private final TarkovMapMapper mapMapper;

    @Override
    public PageResponse<AdminMapExtractResponse> listExtracts(
            Long mapId,
            String status,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<MapExtract> query = new LambdaQueryWrapper<MapExtract>()
                .orderByAsc(MapExtract::getMapId)
                .orderByAsc(MapExtract::getName)
                .orderByAsc(MapExtract::getId);

        if (mapId != null) {
            query.eq(MapExtract::getMapId, mapId);
        }

        if (StringUtils.hasText(status)) {
            query.eq(MapExtract::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(MapExtract::getName, keyword)
                    .or()
                    .like(MapExtract::getFactionLimit, keyword)
                    .or()
                    .like(MapExtract::getConditionText, keyword)
                    .or()
                    .like(MapExtract::getDescription, keyword));
        }

        Page<MapExtract> extractPage = mapExtractMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, extractPage.getTotal(), toResponses(extractPage.getRecords()));
    }

    @Override
    public AdminMapExtractResponse updateExtract(Long id, AdminMapExtractUpdateRequest request) {
        MapExtract extract = mapExtractMapper.selectById(id);
        if (extract == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Map extract does not exist");
        }

        TarkovMap map = mapMapper.selectById(request.mapId());
        if (map == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Map does not exist");
        }

        extract.setMapId(request.mapId());
        extract.setName(request.name().trim());
        extract.setFactionLimit(normalizeNullable(request.factionLimit()));
        extract.setConditionText(normalizeNullable(request.conditionText()));
        extract.setDescription(normalizeNullable(request.description()));
        extract.setStatus(request.status());
        mapExtractMapper.updateById(extract);

        return toResponses(List.of(mapExtractMapper.selectById(id))).get(0);
    }

    private List<AdminMapExtractResponse> toResponses(List<MapExtract> extracts) {
        Map<Long, TarkovMap> maps = selectByIds(
                extracts.stream().map(MapExtract::getMapId).filter(Objects::nonNull).toList(),
                mapMapper::selectBatchIds,
                TarkovMap::getId
        );

        return extracts.stream()
                .map(extract -> toResponse(extract, maps.get(extract.getMapId())))
                .toList();
    }

    private static AdminMapExtractResponse toResponse(MapExtract extract, TarkovMap map) {
        return new AdminMapExtractResponse(
                extract.getId(),
                extract.getMapId(),
                displayName(map == null ? null : map.getNameZh(), map == null ? null : map.getNameEn()),
                extract.getName(),
                extract.getFactionLimit(),
                extract.getConditionText(),
                extract.getDescription(),
                extract.getStatus()
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
