package com.tarkovcommunity.tarkov.dto;

import java.util.List;

public record AmmoDetailResponse(
        Long id,
        String nameEn,
        String nameZh,
        String caliber,
        Integer damage,
        Integer penetration,
        Integer armorDamage,
        String description,
        List<WeaponResponse> compatibleWeapons
) {
}
