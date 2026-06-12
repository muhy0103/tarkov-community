package com.tarkovcommunity.user.dto;

import java.time.LocalDateTime;

public record PublicUserProfileResponse(
        Long id,
        String username,
        String nickname,
        String avatar,
        String role,
        Integer contribution,
        Long postCount,
        Long commentCount,
        String bio,
        String favoriteMaps,
        String playStyle,
        String serverRegion,
        Long followerCount,
        Long followingCount,
        Boolean followedByMe,
        Boolean ownProfile,
        LocalDateTime createdAt
) {
}
