package com.tarkovcommunity.forum.dto;

import jakarta.validation.constraints.NotNull;

public record PostActionRequest(
        @NotNull(message = "用户不能为空")
        Long userId
) {
}
