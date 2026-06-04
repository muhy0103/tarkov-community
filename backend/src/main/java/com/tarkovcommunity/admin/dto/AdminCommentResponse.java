package com.tarkovcommunity.admin.dto;

import java.time.LocalDateTime;

public record AdminCommentResponse(
        Long id,
        Long postId,
        String postTitle,
        String authorNickname,
        Long parentId,
        String content,
        String status,
        Integer likeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
