package com.tarkovcommunity.meta.dto;

public record CategoryResponse(
        Long id,
        String name,
        String code,
        String description,
        String icon
) {
}
