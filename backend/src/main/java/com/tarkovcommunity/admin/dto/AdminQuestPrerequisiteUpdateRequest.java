package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.NotNull;

public record AdminQuestPrerequisiteUpdateRequest(
        @NotNull(message = "Quest is required")
        Long questId,

        @NotNull(message = "Prerequisite quest is required")
        Long prerequisiteQuestId
) {
}
