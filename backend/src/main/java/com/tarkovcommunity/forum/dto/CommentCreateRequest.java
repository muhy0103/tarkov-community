package com.tarkovcommunity.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotNull(message = "用户不能为空")
        Long userId,

        @NotBlank(message = "评论内容不能为空")
        @Size(min = 5, max = 1000, message = "评论内容需要在5到1000个字符之间")
        String content,

        Long parentId,

        Long replyToUserId
) {
}
