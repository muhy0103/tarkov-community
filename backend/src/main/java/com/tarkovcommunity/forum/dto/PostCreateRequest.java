package com.tarkovcommunity.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
        @NotNull(message = "用户不能为空")
        Long userId,

        @NotNull(message = "分区不能为空")
        Long categoryId,

        @NotBlank(message = "标题不能为空")
        @Size(max = 160, message = "标题不能超过160个字符")
        String title,

        @NotBlank(message = "内容不能为空")
        @Size(min = 10, message = "内容至少需要10个字符")
        String content,

        @NotBlank(message = "帖子类型不能为空")
        @Size(max = 40, message = "帖子类型不能超过40个字符")
        String postType,

        @Size(max = 500, message = "封面图地址不能超过500个字符")
        String coverImage
) {
}
