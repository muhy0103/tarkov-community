package com.tarkovcommunity.tarkov.dto;

public record ItemDetailResponse(
        Long id,
        String nameEn,
        String nameZh,
        String itemType,
        String rarity,
        String gridSize,
        Boolean questNeeded,
        Boolean hideoutNeeded,
        Boolean keepSuggestion,
        String description
) {
}
