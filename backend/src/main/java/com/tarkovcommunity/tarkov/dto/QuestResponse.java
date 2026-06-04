package com.tarkovcommunity.tarkov.dto;

public record QuestResponse(
        Long id,
        Long traderId,
        String nameEn,
        String nameZh,
        String questType,
        Long mapId
) {
}
