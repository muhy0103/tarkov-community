package com.tarkovcommunity.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tarkovcommunity.forum.dto.PostActionResponse;
import com.tarkovcommunity.forum.entity.Favorite;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostLike;
import com.tarkovcommunity.forum.mapper.FavoriteMapper;
import com.tarkovcommunity.forum.mapper.PostLikeMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.forum.service.ForumReactionService;
import com.tarkovcommunity.notification.entity.Notification;
import com.tarkovcommunity.notification.mapper.NotificationMapper;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ForumReactionServiceImpl implements ForumReactionService {

    private static final int UNREAD = 0;
    private static final int NOTIFICATION_CONTENT_MAX_LENGTH = 500;
    private static final String POST_LIKE_NOTIFICATION_TYPE = "POST_LIKE";

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final FavoriteMapper favoriteMapper;
    private final SysUserMapper sysUserMapper;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public PostActionResponse toggleLike(Long postId, Long userId) {
        Post post = requireNormalPost(postId);
        requireUser(userId);

        PostLike existing = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId));

        boolean active;
        int count = valueOrZero(post.getLikeCount());
        if (existing == null) {
            PostLike like = new PostLike();
            like.setPostId(postId);
            like.setUserId(userId);
            postLikeMapper.insert(like);
            active = true;
            count += 1;
            createPostLikeNotification(post, userId);
        } else {
            postLikeMapper.deleteById(existing.getId());
            active = false;
            count = Math.max(0, count - 1);
        }

        post.setLikeCount(count);
        postMapper.updateById(post);
        return new PostActionResponse(postId, userId, active, count);
    }

    private void createPostLikeNotification(Post post, Long likerId) {
        if (post.getUserId() == null || Objects.equals(post.getUserId(), likerId)) {
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(post.getUserId());
        notification.setType(POST_LIKE_NOTIFICATION_TYPE);
        notification.setTitle("帖子收到点赞");
        notification.setContent(limit("你的帖子《" + post.getTitle() + "》收到了新的点赞。", NOTIFICATION_CONTENT_MAX_LENGTH));
        notification.setRelatedId(post.getId());
        notification.setReadStatus(UNREAD);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    @Override
    public PostActionResponse toggleFavorite(Long postId, Long userId) {
        Post post = requireNormalPost(postId);
        requireUser(userId);

        Favorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getPostId, postId)
                .eq(Favorite::getUserId, userId));

        boolean active;
        int count = valueOrZero(post.getFavoriteCount());
        if (existing == null) {
            Favorite favorite = new Favorite();
            favorite.setPostId(postId);
            favorite.setUserId(userId);
            favoriteMapper.insert(favorite);
            active = true;
            count += 1;
        } else {
            favoriteMapper.deleteById(existing.getId());
            active = false;
            count = Math.max(0, count - 1);
        }

        post.setFavoriteCount(count);
        postMapper.updateById(post);
        return new PostActionResponse(postId, userId, active, count);
    }

    private Post requireNormalPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !"NORMAL".equals(post.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "帖子不存在");
        }
        return post;
    }

    private void requireUser(Long userId) {
        if (sysUserMapper.selectById(userId) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户不存在");
        }
    }

    private static String limit(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
