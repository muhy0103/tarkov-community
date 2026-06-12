package com.tarkovcommunity.tarkov.dto;

import java.util.List;

public record TarkovMapDetailResponse(
        Long id,
        String nameEn,
        String nameZh,
        String difficulty,
        String description,
        String recommendedLevel,
        List<MapExtractResponse> extracts,
        List<MapLootAreaResponse> lootAreas,
        List<BossResponse> bosses,
        List<QuestResponse> quests
) {
}
