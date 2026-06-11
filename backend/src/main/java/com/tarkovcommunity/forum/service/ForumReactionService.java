package com.tarkovcommunity.forum.service;

import com.tarkovcommunity.forum.dto.CommentActionResponse;
import com.tarkovcommunity.forum.dto.PostActionResponse;

public interface ForumReactionService {

    PostActionResponse toggleLike(Long postId, Long userId);

    PostActionResponse toggleFavorite(Long postId, Long userId);

    CommentActionResponse toggleCommentLike(Long postId, Long commentId, Long userId);
}
