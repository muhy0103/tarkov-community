package com.tarkovcommunity.tarkov.dto;

public record TraderResponse(
        Long id,
        String nameEn,
        String nameZh,
        String unlockCondition,
        String avatar
) {
}
