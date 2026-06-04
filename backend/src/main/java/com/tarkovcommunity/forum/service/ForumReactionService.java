package com.tarkovcommunity.forum.service;

import com.tarkovcommunity.forum.dto.PostActionResponse;

public interface ForumReactionService {

    PostActionResponse toggleLike(Long postId, Long userId);

    PostActionResponse toggleFavorite(Long postId, Long userId);
}
