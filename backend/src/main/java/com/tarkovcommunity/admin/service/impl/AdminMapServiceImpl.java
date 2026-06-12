package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminMapResponse;
import com.tarkovcommunity.admin.dto.AdminMapUpdateRequest;
import com.tarkovcommunity.admin.service.AdminMapService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.mapper.TarkovMapMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMapServiceImpl implements AdminMapService {

    private static final int MAX_PAGE_SIZE = 50;

    private final TarkovMapMapper mapMapper;

    @Override
    public PageResponse<AdminMapResponse> listMaps(String status, String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<TarkovMap> query = new LambdaQueryWrapper<TarkovMap>()
                .orderByAsc(TarkovMap::getId);

        if (StringUtils.hasText(status)) {
            query.eq(TarkovMap::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(TarkovMap::getNameEn, keyword)
                    .or()
                    .like(TarkovMap::getNameZh, keyword)
                    .or()
                    .like(TarkovMap::getDifficulty, keyword)
                    .or()
                    .like(TarkovMap::getDescription, keyword)
                    .or()
                    .like(TarkovMap::getRecommendedLevel, keyword));
        }

        Page<TarkovMap> mapPage = mapMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, mapPage.getTotal(), toResponses(mapPage.getRecords()));
    }

    @Override
    public AdminMapResponse updateMap(Long id, AdminMapUpdateRequest request) {
        TarkovMap map = mapMapper.selectById(id);
        if (map == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "地图不存在");
        }

        map.setNameEn(request.nameEn().trim());
        map.setNameZh(request.nameZh().trim());
        map.setDifficulty(normalizeNullable(request.difficulty()));
        map.setDescription(normalizeNullable(request.description()));
        map.setRecommendedLevel(normalizeNullable(request.recommendedLevel()));
        map.setImageUrl(normalizeNullable(request.imageUrl()));
        map.setStatus(request.status());
        mapMapper.updateById(map);

        return toResponse(mapMapper.selectById(id));
    }

    private static List<AdminMapResponse> toResponses(List<TarkovMap> maps) {
        return maps.stream()
                .map(AdminMapServiceImpl::toResponse)
                .toList();
    }

    private static AdminMapResponse toResponse(TarkovMap map) {
        return new AdminMapResponse(
                map.getId(),
                map.getNameEn(),
                map.getNameZh(),
                map.getDifficulty(),
                map.getDescription(),
                map.getRecommendedLevel(),
                map.getImageUrl(),
                map.getStatus()
        );
    }

    private static String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
