package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminWeaponResponse;
import com.tarkovcommunity.admin.dto.AdminWeaponUpdateRequest;
import com.tarkovcommunity.admin.service.AdminWeaponService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.tarkov.entity.TarkovWeapon;
import com.tarkovcommunity.tarkov.mapper.TarkovWeaponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminWeaponServiceImpl implements AdminWeaponService {

    private static final int MAX_PAGE_SIZE = 50;

    private final TarkovWeaponMapper weaponMapper;

    @Override
    public PageResponse<AdminWeaponResponse> listWeapons(
            String weaponType,
            String caliber,
            String status,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<TarkovWeapon> query = new LambdaQueryWrapper<TarkovWeapon>()
                .orderByAsc(TarkovWeapon::getId);

        if (StringUtils.hasText(weaponType)) {
            query.eq(TarkovWeapon::getWeaponType, weaponType);
        }

        if (StringUtils.hasText(caliber)) {
            query.eq(TarkovWeapon::getCaliber, caliber);
        }

        if (StringUtils.hasText(status)) {
            query.eq(TarkovWeapon::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(TarkovWeapon::getNameEn, keyword)
                    .or()
                    .like(TarkovWeapon::getNameZh, keyword)
                    .or()
                    .like(TarkovWeapon::getWeaponType, keyword)
                    .or()
                    .like(TarkovWeapon::getCaliber, keyword)
                    .or()
                    .like(TarkovWeapon::getDescription, keyword));
        }

        Page<TarkovWeapon> weaponPage = weaponMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, weaponPage.getTotal(), toResponses(weaponPage.getRecords()));
    }

    @Override
    public AdminWeaponResponse updateWeapon(Long id, AdminWeaponUpdateRequest request) {
        TarkovWeapon weapon = weaponMapper.selectById(id);
        if (weapon == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "武器不存在");
        }

        weapon.setNameEn(request.nameEn().trim());
        weapon.setNameZh(normalizeNullable(request.nameZh()));
        weapon.setWeaponType(normalizeNullable(request.weaponType()));
        weapon.setCaliber(normalizeNullable(request.caliber()));
        weapon.setDescription(normalizeNullable(request.description()));
        weapon.setImageUrl(normalizeNullable(request.imageUrl()));
        weapon.setStatus(request.status());
        weaponMapper.updateById(weapon);

        return toResponse(weaponMapper.selectById(id));
    }

    private static List<AdminWeaponResponse> toResponses(List<TarkovWeapon> weapons) {
        return weapons.stream()
                .map(AdminWeaponServiceImpl::toResponse)
                .toList();
    }

    private static AdminWeaponResponse toResponse(TarkovWeapon weapon) {
        return new AdminWeaponResponse(
                weapon.getId(),
                weapon.getNameEn(),
                weapon.getNameZh(),
                weapon.getWeaponType(),
                weapon.getCaliber(),
                weapon.getDescription(),
                weapon.getImageUrl(),
                weapon.getStatus()
        );
    }

    private static String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
