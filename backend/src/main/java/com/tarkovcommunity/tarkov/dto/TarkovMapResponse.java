package com.tarkovcommunity.tarkov.dto;

public record TarkovMapResponse(
        Long id,
        String nameEn,
        String nameZh,
        String difficulty,
        String description,
        String recommendedLevel,
        String imageUrl
) {
}
