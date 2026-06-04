package com.tarkovcommunity.tarkov.dto;

public record ItemResponse(
        Long id,
        String nameEn,
        String nameZh,
        String itemType,
        Boolean questNeeded,
        Boolean hideoutNeeded,
        Boolean keepSuggestion
) {
}
