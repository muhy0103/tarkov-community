package com.tarkovcommunity.admin.dto;

public record AdminCategoryResponse(
        Long id,
        String name,
        String code,
        String description,
        String icon,
        Integer sortOrder,
        String status
) {
}
