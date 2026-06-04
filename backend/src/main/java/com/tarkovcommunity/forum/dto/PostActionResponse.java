package com.tarkovcommunity.forum.dto;

public record PostActionResponse(Long postId, Long userId, Boolean active, Integer count) {
}
