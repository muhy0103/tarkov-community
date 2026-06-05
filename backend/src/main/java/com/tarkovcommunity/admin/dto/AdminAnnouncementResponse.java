package com.tarkovcommunity.admin.dto;

import java.time.LocalDateTime;

public record AdminAnnouncementResponse(
        Long id,
        String title,
        String content,
        String status,
        Long createdBy,
        String creatorNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
