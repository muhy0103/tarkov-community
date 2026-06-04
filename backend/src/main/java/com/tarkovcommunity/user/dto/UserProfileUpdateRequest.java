package com.tarkovcommunity.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @NotBlank(message = "昵称不能为空")
        @Size(max = 50, message = "昵称不能超过50个字符")
        String nickname,

        @Email(message = "邮箱格式不正确")
        @Size(max = 120, message = "邮箱不能超过120个字符")
        String email,

        @Size(max = 500, message = "头像地址不能超过500个字符")
        String avatar
) {
}
