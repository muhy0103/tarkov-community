package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminReportReviewRequest(
        @NotBlank(message = "举报处理状态不能为空")
        @Pattern(regexp = "PENDING|RESOLVED|REJECTED", message = "举报处理状态不正确")
        String status,

        @Size(max = 500, message = "处理说明不能超过500个字符")
        String handleResult
) {
}
