package com.tarkovcommunity.admin.dto;

public record AdminQuestResponse(
        Long id,
        Long traderId,
        String traderName,
        String nameEn,
        String nameZh,
        String questType,
        Long mapId,
        String mapName,
        String description,
        String rewards,
        String unlocks,
        String status
) {
}
