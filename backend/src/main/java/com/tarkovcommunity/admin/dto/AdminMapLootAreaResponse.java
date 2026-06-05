package com.tarkovcommunity.admin.dto;

public record AdminMapLootAreaResponse(
        Long id,
        Long mapId,
        String mapName,
        String name,
        String lootType,
        String riskLevel,
        String description
) {
}
