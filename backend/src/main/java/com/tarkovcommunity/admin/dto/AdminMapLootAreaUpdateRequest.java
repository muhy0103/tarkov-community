package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminMapLootAreaUpdateRequest(
        @NotNull(message = "Map is required")
        Long mapId,

        @NotBlank(message = "Loot area name is required")
        @Size(max = 120, message = "Loot area name must not exceed 120 characters")
        String name,

        @Size(max = 80, message = "Loot type must not exceed 80 characters")
        String lootType,

        @Size(max = 30, message = "Risk level must not exceed 30 characters")
        String riskLevel,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
