package com.tarkovcommunity.admin.dto;

import java.time.LocalDateTime;

public record AdminReportResponse(
        Long id,
        Long reporterId,
        String reporterNickname,
        String targetType,
        Long targetId,
        String targetTitle,
        String targetSummary,
        String reason,
        String description,
        String status,
        Long handlerId,
        String handlerNickname,
        String handleResult,
        LocalDateTime handledAt,
        LocalDateTime createdAt
) {
}
