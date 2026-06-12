package com.tarkovcommunity.tarkov.dto;

import java.util.List;

public record TraderDetailResponse(
        Long id,
        String nameEn,
        String nameZh,
        String description,
        String unlockCondition,
        String avatar,
        List<QuestResponse> quests
) {
}
