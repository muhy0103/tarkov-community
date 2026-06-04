package com.tarkovcommunity.tarkov.dto;

public record WeaponResponse(
        Long id,
        String nameEn,
        String nameZh,
        String weaponType,
        String caliber
) {
}
