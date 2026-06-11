package com.tarkovcommunity.forum.dto;

public record CommentActionResponse(Long commentId, Long userId, Boolean active, Integer count) {
}
