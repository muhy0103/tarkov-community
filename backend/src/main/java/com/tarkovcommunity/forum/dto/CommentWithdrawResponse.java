package com.tarkovcommunity.forum.dto;

public record CommentWithdrawResponse(
        Long id,
        Long postId,
        String status,
        Integer postCommentCount
) {
}
