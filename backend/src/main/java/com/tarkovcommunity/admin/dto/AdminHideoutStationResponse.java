package com.tarkovcommunity.admin.dto;

public record AdminHideoutStationResponse(
        Long id,
        String nameEn,
        String nameZh,
        String description,
        String status
) {
}
