package com.tarkovcommunity.admin.dto;

public record AdminTagResponse(
        Long id,
        String name,
        String type,
        String color,
        String status
) {
}
