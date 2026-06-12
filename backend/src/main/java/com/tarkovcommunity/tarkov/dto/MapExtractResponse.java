package com.tarkovcommunity.tarkov.dto;

public record MapExtractResponse(
        Long id,
        String name,
        String factionLimit,
        String conditionText,
        String description
) {
}
