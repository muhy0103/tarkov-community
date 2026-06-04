package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.Size;

public record AdminCommentReviewRequest(
        @Size(max = 20, message = "评论状态不能超过20个字符")
        String status
) {
}
