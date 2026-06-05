package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminAnnouncementUpsertRequest(
        @NotBlank(message = "公告标题不能为空")
        @Size(max = 120, message = "公告标题不能超过120个字符")
        String title,

        @NotBlank(message = "公告内容不能为空")
        @Size(max = 4000, message = "公告内容不能超过4000个字符")
        String content,

        @NotBlank(message = "公告状态不能为空")
        @Pattern(regexp = "PUBLISHED|DRAFT|ARCHIVED", message = "公告状态不正确")
        String status
) {
}
