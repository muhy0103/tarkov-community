package com.tarkovcommunity.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordUpdateRequest(
        @NotBlank(message = "当前密码不能为空")
        @Size(min = 6, max = 50, message = "当前密码需要在6到50个字符之间")
        String currentPassword,

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 50, message = "新密码需要在6到50个字符之间")
        String newPassword
) {
}
