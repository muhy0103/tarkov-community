package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminHideoutUpgradeUpdateRequest(
        @NotNull(message = "Hideout station is required")
        Long stationId,

        @NotNull(message = "Upgrade level is required")
        @Min(value = 1, message = "Upgrade level must be at least 1")
        Integer level,

        @Size(max = 2000, message = "Required items must not exceed 2000 characters")
        String requiredItems,

        @Size(max = 80, message = "Required time must not exceed 80 characters")
        String requiredTime,

        @Size(max = 500, message = "Unlocks must not exceed 500 characters")
        String unlocks
) {
}
