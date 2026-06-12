package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminAmmoResponse;
import com.tarkovcommunity.admin.dto.AdminAmmoUpdateRequest;
import com.tarkovcommunity.admin.service.AdminAmmoService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.TarkovAmmo;
import com.tarkovcommunity.tarkov.mapper.TarkovAmmoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAmmoServiceImpl implements AdminAmmoService {

    private static final int MAX_PAGE_SIZE = 50;

    private final TarkovAmmoMapper ammoMapper;

    @Override
    public PageResponse<AdminAmmoResponse> listAmmo(String caliber, String status, String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<TarkovAmmo> query = new LambdaQueryWrapper<TarkovAmmo>()
                .orderByAsc(TarkovAmmo::getId);

        if (StringUtils.hasText(caliber)) {
            query.eq(TarkovAmmo::getCaliber, caliber);
        }

        if (StringUtils.hasText(status)) {
            query.eq(TarkovAmmo::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(TarkovAmmo::getNameEn, keyword)
                    .or()
                    .like(TarkovAmmo::getNameZh, keyword)
                    .or()
                    .like(TarkovAmmo::getCaliber, keyword)
                    .or()
                    .like(TarkovAmmo::getDescription, keyword));
        }

        Page<TarkovAmmo> ammoPage = ammoMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, ammoPage.getTotal(), toResponses(ammoPage.getRecords()));
    }

    @Override
    public AdminAmmoResponse updateAmmo(Long id, AdminAmmoUpdateRequest request) {
        TarkovAmmo ammo = ammoMapper.selectById(id);
        if (ammo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "弹药不存在");
        }

        ammo.setNameEn(request.nameEn().trim());
        ammo.setNameZh(normalizeNullable(request.nameZh()));
        ammo.setCaliber(request.caliber().trim());
        ammo.setDamage(request.damage());
        ammo.setPenetration(request.penetration());
        ammo.setArmorDamage(request.armorDamage());
        ammo.setDescription(normalizeNullable(request.description()));
        ammo.setImageUrl(normalizeNullable(request.imageUrl()));
        ammo.setStatus(request.status());
        ammoMapper.updateById(ammo);

        return toResponse(ammoMapper.selectById(id));
    }

    private static List<AdminAmmoResponse> toResponses(List<TarkovAmmo> ammoList) {
        return ammoList.stream()
                .map(AdminAmmoServiceImpl::toResponse)
                .toList();
    }

    private static AdminAmmoResponse toResponse(TarkovAmmo ammo) {
        return new AdminAmmoResponse(
                ammo.getId(),
                ammo.getNameEn(),
                ammo.getNameZh(),
                ammo.getCaliber(),
                ammo.getDamage(),
                ammo.getPenetration(),
                ammo.getArmorDamage(),
                ammo.getDescription(),
                ammo.getImageUrl(),
                ammo.getStatus()
        );
    }

    private static String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
