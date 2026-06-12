package com.tarkovcommunity.tarkov.dto;

public record AmmoResponse(
        Long id,
        String nameEn,
        String nameZh,
        String caliber,
        Integer damage,
        Integer penetration,
        String imageUrl
) {
}
