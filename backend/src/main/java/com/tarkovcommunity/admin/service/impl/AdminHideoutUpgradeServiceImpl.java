package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminHideoutUpgradeResponse;
import com.tarkovcommunity.admin.dto.AdminHideoutUpgradeUpdateRequest;
import com.tarkovcommunity.admin.service.AdminHideoutUpgradeService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.HideoutStation;
import com.tarkovcommunity.tarkov.entity.HideoutUpgrade;
import com.tarkovcommunity.tarkov.mapper.HideoutStationMapper;
import com.tarkovcommunity.tarkov.mapper.HideoutUpgradeMapper;
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
public class AdminHideoutUpgradeServiceImpl implements AdminHideoutUpgradeService {

    private static final int MAX_PAGE_SIZE = 50;

    private final HideoutUpgradeMapper hideoutUpgradeMapper;
    private final HideoutStationMapper hideoutStationMapper;

    @Override
    public PageResponse<AdminHideoutUpgradeResponse> listUpgrades(
            Long stationId,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<HideoutUpgrade> query = new LambdaQueryWrapper<HideoutUpgrade>()
                .orderByAsc(HideoutUpgrade::getStationId)
                .orderByAsc(HideoutUpgrade::getLevel)
                .orderByAsc(HideoutUpgrade::getId);

        if (stationId != null) {
            query.eq(HideoutUpgrade::getStationId, stationId);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(HideoutUpgrade::getRequiredItems, keyword)
                    .or()
                    .like(HideoutUpgrade::getRequiredTime, keyword)
                    .or()
                    .like(HideoutUpgrade::getUnlocks, keyword));
        }

        Page<HideoutUpgrade> upgradePage = hideoutUpgradeMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, upgradePage.getTotal(), toResponses(upgradePage.getRecords()));
    }

    @Override
    public AdminHideoutUpgradeResponse updateUpgrade(Long id, AdminHideoutUpgradeUpdateRequest request) {
        HideoutUpgrade upgrade = hideoutUpgradeMapper.selectById(id);
        if (upgrade == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hideout upgrade does not exist");
        }

        HideoutStation station = hideoutStationMapper.selectById(request.stationId());
        if (station == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hideout station does not exist");
        }

        upgrade.setStationId(request.stationId());
        upgrade.setLevel(request.level());
        upgrade.setRequiredItems(normalizeNullable(request.requiredItems()));
        upgrade.setRequiredTime(normalizeNullable(request.requiredTime()));
        upgrade.setUnlocks(normalizeNullable(request.unlocks()));
        hideoutUpgradeMapper.updateById(upgrade);

        return toResponses(List.of(hideoutUpgradeMapper.selectById(id))).get(0);
    }

    private List<AdminHideoutUpgradeResponse> toResponses(List<HideoutUpgrade> upgrades) {
        Map<Long, HideoutStation> stations = selectByIds(
                upgrades.stream().map(HideoutUpgrade::getStationId).filter(Objects::nonNull).toList(),
                hideoutStationMapper::selectBatchIds,
                HideoutStation::getId
        );

        return upgrades.stream()
                .map(upgrade -> toResponse(upgrade, stations.get(upgrade.getStationId())))
                .toList();
    }

    private static AdminHideoutUpgradeResponse toResponse(HideoutUpgrade upgrade, HideoutStation station) {
        return new AdminHideoutUpgradeResponse(
                upgrade.getId(),
                upgrade.getStationId(),
                displayName(station == null ? null : station.getNameZh(), station == null ? null : station.getNameEn()),
                upgrade.getLevel(),
                upgrade.getRequiredItems(),
                upgrade.getRequiredTime(),
                upgrade.getUnlocks()
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
