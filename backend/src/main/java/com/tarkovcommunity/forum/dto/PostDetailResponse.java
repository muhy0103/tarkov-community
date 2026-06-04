package com.tarkovcommunity.forum.dto;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        String postType,
        String categoryName,
        String categoryCode,
        String authorNickname,
        Boolean recommended,
        Integer viewCount,
        Integer likeCount,
        Integer commentCount,
        LocalDateTime createdAt
) {
}
