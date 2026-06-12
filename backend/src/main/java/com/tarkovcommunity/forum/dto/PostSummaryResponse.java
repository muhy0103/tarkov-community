package com.tarkovcommunity.forum.dto;

import java.time.LocalDateTime;

public record PostSummaryResponse(
        Long id,
        String title,
        String summary,
        String postType,
        String categoryName,
        String categoryCode,
        Long authorId,
        String authorNickname,
        Boolean recommended,
        Integer viewCount,
        Integer likeCount,
        Integer commentCount,
        LocalDateTime createdAt
) {
}
