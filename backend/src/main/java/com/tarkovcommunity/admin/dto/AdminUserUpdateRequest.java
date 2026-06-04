package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.Size;

public record AdminUserUpdateRequest(
        @Size(max = 20, message = "用户角色不能超过20个字符")
        String role,

        @Size(max = 20, message = "用户状态不能超过20个字符")
        String status
) {
}
