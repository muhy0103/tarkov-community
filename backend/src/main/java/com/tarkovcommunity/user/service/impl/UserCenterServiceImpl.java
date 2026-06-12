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
import com.tarkovcommunity.user.dto.UserPasswordUpdateRequest;
import com.tarkovcommunity.user.dto.UserProfileUpdateRequest;
import com.tarkovcommunity.user.dto.UserRelationResponse;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.entity.UserFollow;
import com.tarkovcommunity.user.entity.UserProfile;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import com.tarkovcommunity.user.mapper.UserFollowMapper;
import com.tarkovcommunity.user.mapper.UserProfileMapper;
import com.tarkovcommunity.user.service.UserCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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
    private final UserFollowMapper userFollowMapper;
    private final UserProfileMapper userProfileMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserCenterSummaryResponse getSummary(SysUser user) {
        Long postCount = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, user.getId())
                .eq(Post::getStatus, "NORMAL"));
        Long commentCount = postCommentMapper.selectVisiblePostCommentCount(user.getId());
        Long favoriteCount = favoriteMapper.selectVisiblePostFavoriteCount(user.getId());

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
        Page<PostComment> commentPage = postCommentMapper.selectVisiblePostCommentsPage(new Page<>(safePage, safeSize), user.getId());

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
        Page<Favorite> favoritePage = favoriteMapper.selectVisiblePostFavoritesPage(new Page<>(safePage, safeSize), user.getId());

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

    @Override
    public PageResponse<UserRelationResponse> listFollowing(SysUser user, int page, int size) {
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        Page<UserFollow> followPage = userFollowMapper.selectPage(
                new Page<>(safePage, safeSize),
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getUserId, user.getId())
                        .orderByDesc(UserFollow::getCreatedAt)
                        .orderByDesc(UserFollow::getId)
        );

        return PageResponse.of(
                safePage,
                safeSize,
                followPage.getTotal(),
                toRelationResponses(user.getId(), followPage.getRecords(), UserFollow::getFollowedUserId, true)
        );
    }

    @Override
    public PageResponse<UserRelationResponse> listFollowers(SysUser user, int page, int size) {
        int safePage = safePage(page);
        int safeSize = safeSize(size);
        Page<UserFollow> followPage = userFollowMapper.selectPage(
                new Page<>(safePage, safeSize),
                new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowedUserId, user.getId())
                        .orderByDesc(UserFollow::getCreatedAt)
                        .orderByDesc(UserFollow::getId)
        );

        return PageResponse.of(
                safePage,
                safeSize,
                followPage.getTotal(),
                toRelationResponses(user.getId(), followPage.getRecords(), UserFollow::getUserId, false)
        );
    }

    @Override
    public UserCenterSummaryResponse updateProfile(SysUser user, UserProfileUpdateRequest request) {
        String email = normalizeNullable(request.email());
        if (StringUtils.hasText(email)) {
            SysUser existing = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getEmail, email));
            if (existing != null && !Objects.equals(existing.getId(), user.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "邮箱已被使用");
            }
        }

        user.setNickname(request.nickname().trim());
        user.setEmail(email);
        user.setAvatar(normalizeNullable(request.avatar()));
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);

        return getSummary(user);
    }

    @Override
    public void updatePassword(SysUser user, UserPasswordUpdateRequest request) {
        if (!StringUtils.hasText(user.getPassword()) || !passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前密码不正确");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "新密码不能与当前密码相同");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
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

    private List<UserRelationResponse> toRelationResponses(
            Long currentUserId,
            List<UserFollow> follows,
            Function<UserFollow, Long> relatedUserIdGetter,
            boolean alwaysFollowedByMe
    ) {
        if (follows.isEmpty()) {
            return List.of();
        }

        List<Long> relatedUserIds = follows.stream()
                .map(relatedUserIdGetter)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, SysUser> users = selectByIds(relatedUserIds, sysUserMapper::selectBatchIds, SysUser::getId);
        Map<Long, UserProfile> profiles = selectProfiles(relatedUserIds);

        return follows.stream()
                .map(follow -> {
                    Long relatedUserId = relatedUserIdGetter.apply(follow);
                    SysUser relatedUser = users.get(relatedUserId);
                    if (relatedUser == null || !"NORMAL".equals(relatedUser.getStatus())) {
                        return null;
                    }
                    UserProfile profile = profiles.get(relatedUserId);
                    return new UserRelationResponse(
                            relatedUser.getId(),
                            relatedUser.getUsername(),
                            relatedUser.getNickname(),
                            relatedUser.getAvatar(),
                            relatedUser.getRole(),
                            valueOrZero(relatedUser.getContribution()),
                            profile == null ? null : profile.getBio(),
                            profile == null ? null : profile.getFavoriteMaps(),
                            followerCount(relatedUser.getId()),
                            followingCount(relatedUser.getId()),
                            alwaysFollowedByMe || hasFollowed(currentUserId, relatedUser.getId()),
                            follow.getCreatedAt()
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<Long, UserProfile> selectProfiles(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                        .in(UserProfile::getUserId, userIds))
                .stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
    }

    private Long followerCount(Long userId) {
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowedUserId, userId));
    }

    private Long followingCount(Long userId) {
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, userId));
    }

    private boolean hasFollowed(Long currentUserId, Long targetUserId) {
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, currentUserId)
                .eq(UserFollow::getFollowedUserId, targetUserId)) > 0;
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
                            post.getUserId(),
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

    private static String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
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
