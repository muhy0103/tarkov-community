package com.tarkovcommunity.user.dto;

import java.time.LocalDateTime;

public record UserCenterCommentResponse(
        Long id,
        Long postId,
        String postTitle,
        String content,
        String status,
        Integer likeCount,
        LocalDateTime createdAt
) {
}
