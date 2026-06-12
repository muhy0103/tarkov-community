package com.tarkovcommunity.tarkov.dto;

import java.util.List;

public record HideoutStationDetailResponse(
        Long id,
        String nameEn,
        String nameZh,
        String description,
        List<HideoutUpgradeResponse> upgrades
) {
}
