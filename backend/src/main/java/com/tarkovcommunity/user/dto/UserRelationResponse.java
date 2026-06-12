package com.tarkovcommunity.user.dto;

import java.time.LocalDateTime;

public record UserRelationResponse(
        Long id,
        String username,
        String nickname,
        String avatar,
        String role,
        Integer contribution,
        String bio,
        String favoriteMaps,
        Long followerCount,
        Long followingCount,
        Boolean followedByMe,
        LocalDateTime followedAt
) {
}
