package com.tarkovcommunity.admin.dto;

import java.time.LocalDateTime;

public record AdminPostResponse(
        Long id,
        String title,
        String postType,
        String categoryName,
        String categoryCode,
        String authorNickname,
        String status,
        Boolean recommended,
        Boolean pinned,
        Integer viewCount,
        Integer likeCount,
        Integer commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
