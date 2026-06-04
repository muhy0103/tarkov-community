package com.tarkovcommunity.admin.dto;

public record AdminWeaponResponse(
        Long id,
        String nameEn,
        String nameZh,
        String weaponType,
        String caliber,
        String description,
        String status
) {
}
