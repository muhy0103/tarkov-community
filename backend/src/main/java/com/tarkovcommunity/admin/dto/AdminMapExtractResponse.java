package com.tarkovcommunity.admin.dto;

public record AdminMapExtractResponse(
        Long id,
        Long mapId,
        String mapName,
        String name,
        String factionLimit,
        String conditionText,
        String description,
        String status
) {
}
