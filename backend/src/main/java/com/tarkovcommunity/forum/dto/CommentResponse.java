package com.tarkovcommunity.forum.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long postId,
        Long userId,
        String authorNickname,
        Long parentId,
        Long replyToUserId,
        String content,
        Integer likeCount,
        LocalDateTime createdAt
) {
}
