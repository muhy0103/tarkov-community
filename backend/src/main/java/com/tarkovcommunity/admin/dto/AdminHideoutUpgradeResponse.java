package com.tarkovcommunity.admin.dto;

public record AdminHideoutUpgradeResponse(
        Long id,
        Long stationId,
        String stationName,
        Integer level,
        String requiredItems,
        String requiredTime,
        String unlocks
) {
}
