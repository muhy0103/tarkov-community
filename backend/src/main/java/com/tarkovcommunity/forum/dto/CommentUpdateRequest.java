package com.tarkovcommunity.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(
        @NotBlank(message = "\u8bc4\u8bba\u5185\u5bb9\u4e0d\u80fd\u4e3a\u7a7a")
        @Size(min = 5, max = 1000, message = "\u8bc4\u8bba\u5185\u5bb9\u9700\u8981\u57285\u52301000\u4e2a\u5b57\u7b26\u4e4b\u95f4")
        String content
) {
}
