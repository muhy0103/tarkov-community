package com.tarkovcommunity.tarkov.dto;

public record BossDetailResponse(
        Long id,
        String nameEn,
        String nameZh,
        Long mapId,
        String description,
        String equipmentSummary,
        TarkovMapResponse map
) {
}
