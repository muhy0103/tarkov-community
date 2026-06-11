package com.tarkovcommunity.forum.dto;

import java.time.LocalDateTime;

public record CommentUpdatedResponse(
        Long id,
        Long postId,
        String content,
        LocalDateTime updatedAt
) {
}
