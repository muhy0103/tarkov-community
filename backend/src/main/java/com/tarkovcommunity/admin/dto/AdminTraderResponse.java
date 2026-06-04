package com.tarkovcommunity.admin.dto;

public record AdminTraderResponse(
        Long id,
        String nameEn,
        String nameZh,
        String description,
        String unlockCondition,
        String avatar,
        String status
) {
}
