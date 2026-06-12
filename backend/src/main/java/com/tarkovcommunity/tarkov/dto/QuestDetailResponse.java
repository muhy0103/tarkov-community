package com.tarkovcommunity.tarkov.dto;

import java.util.List;

public record QuestDetailResponse(
        Long id,
        Long traderId,
        String nameEn,
        String nameZh,
        String questType,
        Long mapId,
        String description,
        String rewards,
        String unlocks,
        TraderResponse trader,
        TarkovMapResponse map,
        List<QuestResponse> prerequisites
) {
}
