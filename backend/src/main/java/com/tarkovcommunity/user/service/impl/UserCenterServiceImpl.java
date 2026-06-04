package com.tarkovcommunity.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.forum.entity.Favorite;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostComment;
import com.tarkovcommunity.forum.mapper.FavoriteMapper;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.meta.entity.Category;
import com.tarkovcommunity.meta.mapper.CategoryMapper;
import com.tarkovcommunity.user.dto.UserCenterCommentResponse;
import com.tarkovcommunity.user.dto.UserCenterSummaryResponse;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import com.tarkovcommunity.user.service.UserCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCenterServiceImpl implements UserCenterService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PostMapper postMapper;
    private final PostCommentMapper postCommentMapper;
    private final FavoriteMapper favoriteMapper;
    private final CategoryMapper categoryMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public UserCenterSummaryResponse getSummary(SysUser user) {
        Long postCount = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, user.getId())
                .eq(Post::getStatus, "NORMAL"));
        Long commentCount = postCommentMapper.selectCount(new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getUserId, user.getId()));
        Long favoriteCount = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, user.getId()));

        return new UserCenterSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getAvatar(),
                user.getRole(),
                user.getStatus(),
                valueOrZero(user.getContribution()),
                postCount,
                commentCount,
                favoriteCount,
                user.getCreatedAt()
        );
    }

    @Override
    public PageResponse<PostSummaryResponse> listPosts(SysUser user, int page, int size) {
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        Page<Post> postPage = postMapper.selectPage(
                new Page<>(safePage, safeSize),
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getUserId, user.getId())
                        .eq(Post::getStatus, "NORMAL")
                        .orderByDesc(Post::getCreatedAt)
                        .orderByDesc(Post::getId)
        );

        return PageResponse.of(
                safePage,
                safeSize,
                postPage.getTotal(),
                toPostSummaries(postPage.getRecords())
        );
    }

    @Override
    public PageResponse<UserCenterCommentResponse> listComments(SysUser user, int page, int size) {
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        Page<PostComment> commentPage = postCommentMapper.selectPage(
                new Page<>(safePage, safeSize),
                new LambdaQueryWrapper<PostComment>()
                        .eq(PostComment::getUserId, user.getId())
                        .orderByDesc(PostComment::getCreatedAt)
                        .orderByDesc(PostComment::getId)
        );

        return PageResponse.of(
                safePage,
                safeSize,
                commentPage.getTotal(),
                toCommentResponses(commentPage.getRecords())
        );
    }

    @Override
    public PageResponse<PostSummaryResponse> listFavorites(SysUser user, int page, int size) {
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        Page<Favorite> favoritePage = favoriteMapper.selectPage(
                new Page<>(safePage, safeSize),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, user.getId())
                        .orderByDesc(Favorite::getCreatedAt)
                        .orderByDesc(Favorite::getId)
        );

        List<Long> postIds = favoritePage.getRecords()
                .stream()
                .map(Favorite::getPostId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, Post> posts = selectByIds(postIds, postMapper::selectBatchIds, Post::getId);
        List<Post> orderedPosts = postIds.stream()
                .map(posts::get)
                .filter(Objects::nonNull)
                .filter(post -> "NORMAL".equals(post.getStatus()))
                .toList();

        return PageResponse.of(
                safePage,
                safeSize,
                favoritePage.getTotal(),
                toPostSummaries(orderedPosts)
        );
    }

    private List<UserCenterCommentResponse> toCommentResponses(List<PostComment> comments) {
        if (comments.isEmpty()) {
            return List.of();
        }

        Map<Long, Post> posts = selectByIds(
                comments.stream().map(PostComment::getPostId).filter(Objects::nonNull).toList(),
                postMapper::selectBatchIds,
                Post::getId
        );

        return comments.stream()
                .map(comment -> {
                    Post post = posts.get(comment.getPostId());
                    return new UserCenterCommentResponse(
                            comment.getId(),
                            comment.getPostId(),
                            postTitle(post),
                            comment.getContent(),
                            comment.getStatus(),
                            valueOrZero(comment.getLikeCount()),
                            comment.getCreatedAt()
                    );
                })
                .toList();
    }

    private List<PostSummaryResponse> toPostSummaries(List<Post> posts) {
        if (posts.isEmpty()) {
            return List.of();
        }

        Map<Long, Category> categories = selectByIds(
                posts.stream().map(Post::getCategoryId).filter(Objects::nonNull).toList(),
                categoryMapper::selectBatchIds,
                Category::getId
        );
        Map<Long, SysUser> users = selectByIds(
                posts.stream().map(Post::getUserId).filter(Objects::nonNull).toList(),
                sysUserMapper::selectBatchIds,
                SysUser::getId
        );

        return posts.stream()
                .map(post -> {
                    Category category = categories.get(post.getCategoryId());
                    SysUser author = users.get(post.getUserId());
                    return new PostSummaryResponse(
                            post.getId(),
                            post.getTitle(),
                            excerpt(post.getContent()),
                            post.getPostType(),
                            categoryName(category),
                            categoryCode(category),
                            authorNickname(author),
                            isRecommended(post),
                            valueOrZero(post.getViewCount()),
                            valueOrZero(post.getLikeCount()),
                            valueOrZero(post.getCommentCount()),
                            post.getCreatedAt()
                    );
                })
                .toList();
    }

    private static <T> Map<Long, T> selectByIds(
            List<Long> ids,
            Function<List<Long>, List<T>> selector,
            Function<T, Long> idGetter
    ) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return selector.apply(ids.stream().distinct().toList())
                .stream()
                .collect(Collectors.toMap(idGetter, Function.identity()));
    }

    private static int safePage(int page) {
        return Math.max(page, 1);
    }

    private static int safeSize(int size) {
        return Math.max(1, Math.min(size, MAX_PAGE_SIZE));
    }

    private static String excerpt(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        return normalized.length() > 90 ? normalized.substring(0, 90) + "..." : normalized;
    }

    private static String categoryName(Category category) {
        return category == null ? "Unknown category" : category.getName();
    }

    private static String categoryCode(Category category) {
        return category == null ? "unknown" : category.getCode();
    }

    private static String authorNickname(SysUser author) {
        return author == null ? "Unknown player" : author.getNickname();
    }

    private static String postTitle(Post post) {
        return post == null ? "Unknown post" : post.getTitle();
    }

    private static boolean isRecommended(Post post) {
        return post.getRecommended() != null && post.getRecommended() == 1;
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
