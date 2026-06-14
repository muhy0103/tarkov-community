package com.tarkovcommunity.forum.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostUpdateRequest(
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
        String coverImage,

        @Size(max = 6, message = "最多关联 6 个资料")
        @Valid
        List<PostCatalogRelationRequest> relations
) {
}
