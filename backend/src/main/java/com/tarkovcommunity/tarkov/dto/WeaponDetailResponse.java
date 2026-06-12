package com.tarkovcommunity.tarkov.dto;

import java.util.List;

public record WeaponDetailResponse(
        Long id,
        String nameEn,
        String nameZh,
        String weaponType,
        String caliber,
        String description,
        List<AmmoResponse> compatibleAmmo
) {
}
