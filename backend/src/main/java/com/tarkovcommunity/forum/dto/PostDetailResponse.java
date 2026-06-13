package com.tarkovcommunity.forum.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long id,
        Long authorId,
        String title,
        String content,
        String postType,
        Long categoryId,
        String categoryName,
        String categoryCode,
        String authorNickname,
        Boolean recommended,
        Integer viewCount,
        Integer likeCount,
        Integer commentCount,
        LocalDateTime createdAt,
        Integer favoriteCount,
        Boolean likedByCurrentUser,
        Boolean favoritedByCurrentUser,
        List<RelatedCatalogResponse> relations
) {
}
