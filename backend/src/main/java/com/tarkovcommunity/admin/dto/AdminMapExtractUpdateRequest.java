package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminMapExtractUpdateRequest(
        @NotNull(message = "Map is required")
        Long mapId,

        @NotBlank(message = "Extract name is required")
        @Size(max = 120, message = "Extract name must not exceed 120 characters")
        String name,

        @Size(max = 40, message = "Faction limit must not exceed 40 characters")
        String factionLimit,

        @Size(max = 255, message = "Condition text must not exceed 255 characters")
        String conditionText,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotBlank(message = "Extract status is required")
        @Pattern(regexp = "ENABLED|DISABLED", message = "Extract status is invalid")
        String status
) {
}
