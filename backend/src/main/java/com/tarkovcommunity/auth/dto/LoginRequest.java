package com.tarkovcommunity.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, message = "密码至少需要6位")
        String password
) {
}
