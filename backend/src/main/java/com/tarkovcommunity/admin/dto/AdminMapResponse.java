package com.tarkovcommunity.admin.dto;

public record AdminMapResponse(
        Long id,
        String nameEn,
        String nameZh,
        String difficulty,
        String description,
        String recommendedLevel,
        String status
) {
}
