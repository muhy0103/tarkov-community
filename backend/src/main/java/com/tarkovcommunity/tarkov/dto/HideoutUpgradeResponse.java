package com.tarkovcommunity.tarkov.dto;

public record HideoutUpgradeResponse(
        Long id,
        Integer level,
        String requiredItems,
        String requiredTime,
        String unlocks
) {
}
