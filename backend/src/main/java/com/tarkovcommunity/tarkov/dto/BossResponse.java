package com.tarkovcommunity.tarkov.dto;

public record BossResponse(
        Long id,
        String nameEn,
        String nameZh,
        Long mapId,
        String imageUrl
) {
}
