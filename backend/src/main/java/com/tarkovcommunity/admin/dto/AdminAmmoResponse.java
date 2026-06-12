package com.tarkovcommunity.admin.dto;

public record AdminAmmoResponse(
        Long id,
        String nameEn,
        String nameZh,
        String caliber,
        Integer damage,
        Integer penetration,
        Integer armorDamage,
        String description,
        String imageUrl,
        String status
) {
}
