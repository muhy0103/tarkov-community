package com.tarkovcommunity.user.dto;

public record FollowActionResponse(
        Long targetUserId,
        Long viewerId,
        Boolean followed,
        Long followerCount,
        Long followingCount
) {
}
