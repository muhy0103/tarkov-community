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
import com.tarkovcommunity.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ForumReactionServiceImpl implements ForumReactionService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final FavoriteMapper favoriteMapper;
    private final SysUserMapper sysUserMapper;

    @Override
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
        } else {
            postLikeMapper.deleteById(existing.getId());
            active = false;
            count = Math.max(0, count - 1);
        }

        post.setLikeCount(count);
        postMapper.updateById(post);
        return new PostActionResponse(postId, userId, active, count);
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

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
