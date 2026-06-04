package com.tarkovcommunity.admin.dto;

public record AdminItemResponse(
        Long id,
        String nameEn,
        String nameZh,
        String itemType,
        String rarity,
        String gridSize,
        Boolean questNeeded,
        Boolean hideoutNeeded,
        Boolean keepSuggestion,
        String description,
        String status
) {
}
