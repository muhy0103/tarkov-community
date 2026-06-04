package com.tarkovcommunity.admin.dto;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String username,
        String nickname,
        String email,
        String role,
        String status,
        Integer contribution,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
