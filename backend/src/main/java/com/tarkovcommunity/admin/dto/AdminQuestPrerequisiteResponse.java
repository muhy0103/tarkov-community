package com.tarkovcommunity.admin.dto;

public record AdminQuestPrerequisiteResponse(
        Long id,
        Long questId,
        String questName,
        Long prerequisiteQuestId,
        String prerequisiteQuestName
) {
}
