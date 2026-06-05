package com.tarkovcommunity.admin.dto;

public record AdminBossResponse(
        Long id,
        String nameEn,
        String nameZh,
        Long mapId,
        String mapName,
        String description,
        String equipmentSummary,
        String status
) {
}
