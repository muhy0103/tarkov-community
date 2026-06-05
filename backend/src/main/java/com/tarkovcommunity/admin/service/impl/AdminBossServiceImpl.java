package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminBossResponse;
import com.tarkovcommunity.admin.dto.AdminBossUpdateRequest;
import com.tarkovcommunity.admin.service.AdminBossService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.Boss;
import com.tarkovcommunity.tarkov.entity.TarkovMap;
import com.tarkovcommunity.tarkov.mapper.BossMapper;
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
public class AdminBossServiceImpl implements AdminBossService {

    private static final int MAX_PAGE_SIZE = 50;

    private final BossMapper bossMapper;
    private final TarkovMapMapper mapMapper;

    @Override
    public PageResponse<AdminBossResponse> listBosses(
            Long mapId,
            String status,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<Boss> query = new LambdaQueryWrapper<Boss>()
                .orderByAsc(Boss::getId);

        if (mapId != null) {
            query.eq(Boss::getMapId, mapId);
        }

        if (StringUtils.hasText(status)) {
            query.eq(Boss::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(Boss::getNameEn, keyword)
                    .or()
                    .like(Boss::getNameZh, keyword)
                    .or()
                    .like(Boss::getDescription, keyword)
                    .or()
                    .like(Boss::getEquipmentSummary, keyword));
        }

        Page<Boss> bossPage = bossMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, bossPage.getTotal(), toResponses(bossPage.getRecords()));
    }

    @Override
    public AdminBossResponse updateBoss(Long id, AdminBossUpdateRequest request) {
        Boss boss = bossMapper.selectById(id);
        if (boss == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Boss 不存在");
        }

        if (request.mapId() != null && mapMapper.selectById(request.mapId()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "所属地图不存在");
        }

        boss.setNameEn(request.nameEn().trim());
        boss.setNameZh(normalizeNullable(request.nameZh()));
        boss.setMapId(request.mapId());
        boss.setDescription(normalizeNullable(request.description()));
        boss.setEquipmentSummary(normalizeNullable(request.equipmentSummary()));
        boss.setStatus(request.status());
        bossMapper.updateById(boss);

        return toResponses(List.of(bossMapper.selectById(id))).get(0);
    }

    private List<AdminBossResponse> toResponses(List<Boss> bosses) {
        Map<Long, TarkovMap> maps = selectByIds(
                bosses.stream().map(Boss::getMapId).filter(Objects::nonNull).toList(),
                mapMapper::selectBatchIds,
                TarkovMap::getId
        );

        return bosses.stream()
                .map(boss -> toResponse(boss, maps.get(boss.getMapId())))
                .toList();
    }

    private static AdminBossResponse toResponse(Boss boss, TarkovMap map) {
        return new AdminBossResponse(
                boss.getId(),
                boss.getNameEn(),
                boss.getNameZh(),
                boss.getMapId(),
                displayName(map == null ? null : map.getNameZh(), map == null ? null : map.getNameEn()),
                boss.getDescription(),
                boss.getEquipmentSummary(),
                boss.getStatus()
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
