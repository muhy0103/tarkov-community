package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminTraderResponse;
import com.tarkovcommunity.admin.dto.AdminTraderUpdateRequest;
import com.tarkovcommunity.admin.service.AdminTraderService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.TarkovTrader;
import com.tarkovcommunity.tarkov.mapper.TarkovTraderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminTraderServiceImpl implements AdminTraderService {

    private static final int MAX_PAGE_SIZE = 50;

    private final TarkovTraderMapper traderMapper;

    @Override
    public PageResponse<AdminTraderResponse> listTraders(String status, String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<TarkovTrader> query = new LambdaQueryWrapper<TarkovTrader>()
                .orderByAsc(TarkovTrader::getId);

        if (StringUtils.hasText(status)) {
            query.eq(TarkovTrader::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(TarkovTrader::getNameEn, keyword)
                    .or()
                    .like(TarkovTrader::getNameZh, keyword)
                    .or()
                    .like(TarkovTrader::getDescription, keyword)
                    .or()
                    .like(TarkovTrader::getUnlockCondition, keyword));
        }

        Page<TarkovTrader> traderPage = traderMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, traderPage.getTotal(), toResponses(traderPage.getRecords()));
    }

    @Override
    public AdminTraderResponse updateTrader(Long id, AdminTraderUpdateRequest request) {
        TarkovTrader trader = traderMapper.selectById(id);
        if (trader == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商人不存在");
        }

        String nameEn = request.nameEn().trim();
        trader.setNameEn(nameEn);
        trader.setNameZh(nameEn);
        trader.setDescription(normalizeNullable(request.description()));
        trader.setUnlockCondition(normalizeNullable(request.unlockCondition()));
        trader.setAvatar(normalizeNullable(request.avatar()));
        trader.setStatus(request.status());
        traderMapper.updateById(trader);

        return toResponse(traderMapper.selectById(id));
    }

    private static List<AdminTraderResponse> toResponses(List<TarkovTrader> traders) {
        return traders.stream()
                .map(AdminTraderServiceImpl::toResponse)
                .toList();
    }

    private static AdminTraderResponse toResponse(TarkovTrader trader) {
        return new AdminTraderResponse(
                trader.getId(),
                trader.getNameEn(),
                null,
                trader.getDescription(),
                trader.getUnlockCondition(),
                trader.getAvatar(),
                trader.getStatus()
        );
    }

    private static String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
