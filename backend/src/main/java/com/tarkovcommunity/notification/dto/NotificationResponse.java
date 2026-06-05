package com.tarkovcommunity.notification.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,
        String title,
        String content,
        Long relatedId,
        Integer readStatus,
        LocalDateTime createdAt
) {
}
