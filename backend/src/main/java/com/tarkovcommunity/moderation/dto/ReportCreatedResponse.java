package com.tarkovcommunity.moderation.dto;

import java.time.LocalDateTime;

public record ReportCreatedResponse(
        Long id,
        String targetType,
        Long targetId,
        String reason,
        String status,
        LocalDateTime createdAt
) {
}
