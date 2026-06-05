package com.tarkovcommunity.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(
        @NotBlank(message = "举报目标类型不能为空")
        @Pattern(regexp = "POST|COMMENT", message = "举报目标类型不正确")
        String targetType,

        @NotNull(message = "举报目标不能为空")
        Long targetId,

        @NotBlank(message = "举报原因不能为空")
        @Size(max = 120, message = "举报原因不能超过120个字符")
        String reason,

        @Size(max = 500, message = "举报说明不能超过500个字符")
        String description
) {
}
