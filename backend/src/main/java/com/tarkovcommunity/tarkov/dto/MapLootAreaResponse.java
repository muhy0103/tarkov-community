package com.tarkovcommunity.tarkov.dto;

public record MapLootAreaResponse(
        Long id,
        String name,
        String lootType,
        String riskLevel,
        String description
) {
}
