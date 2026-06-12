package com.tarkovcommunity.tarkov.dto;

public record TarkovMapResponse(
        Long id,
        String nameEn,
        String nameZh,
        String difficulty,
        String recommendedLevel,
        String imageUrl
) {
}
