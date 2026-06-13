package com.tarkovcommunity.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.meta.entity.Category;
import com.tarkovcommunity.meta.mapper.CategoryMapper;
import com.tarkovcommunity.user.dto.FollowActionResponse;
import com.tarkovcommunity.user.dto.PublicUserProfileResponse;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.entity.UserFollow;
import com.tarkovcommunity.user.entity.UserProfile;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import com.tarkovcommunity.user.mapper.UserFollowMapper;
import com.tarkovcommunity.user.mapper.UserProfileMapper;
import com.tarkovcommunity.user.service.UserPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPublicServiceImpl implements UserPublicService {

    private static final int MAX_PAGE_SIZE = 50;

    private final SysUserMapper sysUserMapper;
    private final PostMapper postMapper;
    private final PostCommentMapper postCommentMapper;
    private final CategoryMapper categoryMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserFollowMapper userFollowMapper;

    @Override
    public PublicUserProfileResponse getProfile(Long userId, SysUser viewer) {
        SysUser user = requireNormalUser(userId);
        Long postCount = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, user.getId())
                .eq(Post::getStatus, "NORMAL"));
        Long commentCount = postCommentMapper.selectVisiblePostCommentCount(user.getId());
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, user.getId()));
        Long followerCount = followerCount(user.getId());
        Long followingCount = followingCount(user.getId());
        boolean ownProfile = isOwnProfile(user, viewer);
        boolean followedByMe = viewer != null
                && viewer.getId() != null
                && !ownProfile
                && hasFollowed(viewer.getId(), user.getId());

        return new PublicUserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getRole(),
                valueOrZero(user.getContribution()),
                postCount,
                commentCount,
                profile == null ? null : profile.getBio(),
                profile == null ? null : profile.getFavoriteMaps(),
                profile == null ? null : profile.getPlayStyle(),
                profile == null ? null : profile.getServerRegion(),
                followerCount,
                followingCount,
                followedByMe,
                ownProfile,
                user.getCreatedAt()
        );
    }

    @Override
    public FollowActionResponse followUser(Long targetUserId, SysUser viewer) {
        requireViewer(viewer);
        rejectSelfFollow(targetUserId, viewer);
        SysUser target = requireNormalUser(targetUserId);

        if (!hasFollowed(viewer.getId(), target.getId())) {
            UserFollow follow = new UserFollow();
            follow.setUserId(viewer.getId());
            follow.setFollowedUserId(target.getId());
            userFollowMapper.insert(follow);
        }

        return followResponse(target.getId(), viewer.getId(), true);
    }

    @Override
    public FollowActionResponse unfollowUser(Long targetUserId, SysUser viewer) {
        requireViewer(viewer);
        rejectSelfFollow(targetUserId, viewer);
        SysUser target = requireNormalUser(targetUserId);

        userFollowMapper.delete(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, viewer.getId())
                .eq(UserFollow::getFollowedUserId, target.getId()));

        return followResponse(target.getId(), viewer.getId(), false);
    }

    @Override
    public PageResponse<PostSummaryResponse> listPosts(Long userId, int page, int size) {
        SysUser user = requireNormalUser(userId);
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

    private SysUser requireNormalUser(Long userId) {
        SysUser user = userId == null ? null : sysUserMapper.selectById(userId);
        if (user == null || !"NORMAL".equals(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "玩家不存在");
        }
        return user;
    }

    private void requireViewer(SysUser viewer) {
        if (viewer == null || viewer.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
    }

    private void rejectSelfFollow(Long targetUserId, SysUser viewer) {
        if (Objects.equals(targetUserId, viewer.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能关注自己");
        }
    }

    private FollowActionResponse followResponse(Long targetUserId, Long viewerId, boolean followed) {
        return new FollowActionResponse(
                targetUserId,
                viewerId,
                followed,
                followerCount(targetUserId),
                followingCount(viewerId)
        );
    }

    private Long followerCount(Long userId) {
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowedUserId, userId));
    }

    private Long followingCount(Long userId) {
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, userId));
    }

    private boolean hasFollowed(Long viewerId, Long targetUserId) {
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getUserId, viewerId)
                .eq(UserFollow::getFollowedUserId, targetUserId)) > 0;
    }

    private static boolean isOwnProfile(SysUser user, SysUser viewer) {
        return viewer != null && Objects.equals(viewer.getId(), user.getId());
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
                            post.getCreatedAt(),
                            List.of()
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

    private static boolean isRecommended(Post post) {
        return post.getRecommended() != null && post.getRecommended() == 1;
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
