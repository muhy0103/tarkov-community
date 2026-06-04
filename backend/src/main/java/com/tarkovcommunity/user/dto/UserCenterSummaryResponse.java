package com.tarkovcommunity.user.dto;

import java.time.LocalDateTime;

public record UserCenterSummaryResponse(
        Long id,
        String username,
        String nickname,
        String email,
        String avatar,
        String role,
        String status,
        Integer contribution,
        Long postCount,
        Long commentCount,
        Long favoriteCount,
        LocalDateTime createdAt
) {
}
