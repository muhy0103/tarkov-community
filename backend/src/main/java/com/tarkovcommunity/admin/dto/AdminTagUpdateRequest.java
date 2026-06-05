package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminTagUpdateRequest(
        @NotBlank(message = "标签名称不能为空")
        @Size(max = 50, message = "标签名称不能超过50个字符")
        String name,

        @NotBlank(message = "标签类型不能为空")
        @Size(max = 30, message = "标签类型不能超过30个字符")
        String type,

        @Pattern(regexp = "^$|#[0-9A-Fa-f]{6}$", message = "标签颜色需要使用 #RRGGBB 格式")
        String color,

        @NotBlank(message = "标签状态不能为空")
        @Pattern(regexp = "ENABLED|DISABLED", message = "标签状态不正确")
        String status
) {
}
