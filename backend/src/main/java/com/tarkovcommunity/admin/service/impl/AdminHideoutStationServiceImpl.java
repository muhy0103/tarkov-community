package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminHideoutStationResponse;
import com.tarkovcommunity.admin.dto.AdminHideoutStationUpdateRequest;
import com.tarkovcommunity.admin.service.AdminHideoutStationService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.HideoutStation;
import com.tarkovcommunity.tarkov.mapper.HideoutStationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminHideoutStationServiceImpl implements AdminHideoutStationService {

    private static final int MAX_PAGE_SIZE = 50;

    private final HideoutStationMapper hideoutStationMapper;

    @Override
    public PageResponse<AdminHideoutStationResponse> listStations(
            String status,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<HideoutStation> query = new LambdaQueryWrapper<HideoutStation>()
                .orderByAsc(HideoutStation::getId);

        if (StringUtils.hasText(status)) {
            query.eq(HideoutStation::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(HideoutStation::getNameEn, keyword)
                    .or()
                    .like(HideoutStation::getNameZh, keyword)
                    .or()
                    .like(HideoutStation::getDescription, keyword));
        }

        Page<HideoutStation> stationPage = hideoutStationMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, stationPage.getTotal(), toResponses(stationPage.getRecords()));
    }

    @Override
    public AdminHideoutStationResponse updateStation(Long id, AdminHideoutStationUpdateRequest request) {
        HideoutStation station = hideoutStationMapper.selectById(id);
        if (station == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "藏身处设施不存在");
        }

        station.setNameEn(request.nameEn().trim());
        station.setNameZh(normalizeNullable(request.nameZh()));
        station.setDescription(normalizeNullable(request.description()));
        station.setStatus(request.status());
        hideoutStationMapper.updateById(station);

        return toResponse(hideoutStationMapper.selectById(id));
    }

    private static List<AdminHideoutStationResponse> toResponses(List<HideoutStation> stations) {
        return stations.stream()
                .map(AdminHideoutStationServiceImpl::toResponse)
                .toList();
    }

    private static AdminHideoutStationResponse toResponse(HideoutStation station) {
        return new AdminHideoutStationResponse(
                station.getId(),
                station.getNameEn(),
                station.getNameZh(),
                station.getDescription(),
                station.getStatus()
        );
    }

    private static String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
