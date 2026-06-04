package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminPostResponse;
import com.tarkovcommunity.admin.dto.AdminPostReviewRequest;
import com.tarkovcommunity.admin.service.AdminPostService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.meta.entity.Category;
import com.tarkovcommunity.meta.mapper.CategoryMapper;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPostServiceImpl implements AdminPostService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> ALLOWED_STATUSES = Set.of("NORMAL", "HIDDEN", "PENDING", "DELETED");

    private final PostMapper postMapper;
    private final CategoryMapper categoryMapper;
    private final SysUserMapper userMapper;

    @Override
    public PageResponse<AdminPostResponse> listPosts(
            String status,
            String categoryCode,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<Post> query = new LambdaQueryWrapper<Post>()
                .orderByDesc(Post::getPinned)
                .orderByDesc(Post::getRecommended)
                .orderByDesc(Post::getCreatedAt);

        if (StringUtils.hasText(status)) {
            query.eq(Post::getStatus, status);
        }

        if (StringUtils.hasText(categoryCode)) {
            Category category = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                    .eq(Category::getCode, categoryCode));
            if (category == null) {
                return PageResponse.of(safePage, safeSize, 0, List.of());
            }
            query.eq(Post::getCategoryId, category.getId());
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper
                    .like(Post::getTitle, keyword)
                    .or()
                    .like(Post::getContent, keyword));
        }

        Page<Post> postPage = postMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, postPage.getTotal(), toResponses(postPage.getRecords()));
    }

    @Override
    public AdminPostResponse reviewPost(Long id, AdminPostReviewRequest request) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "帖子不存在");
        }

        if (StringUtils.hasText(request.status())) {
            if (!ALLOWED_STATUSES.contains(request.status())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "帖子状态不正确");
            }
            post.setStatus(request.status());
        }

        if (request.recommended() != null) {
            post.setRecommended(request.recommended() ? 1 : 0);
        }

        if (request.pinned() != null) {
            post.setPinned(request.pinned() ? 1 : 0);
        }

        postMapper.updateById(post);
        return toResponses(List.of(postMapper.selectById(id))).get(0);
    }

    private List<AdminPostResponse> toResponses(List<Post> posts) {
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
                userMapper::selectBatchIds,
                SysUser::getId
        );

        return posts.stream()
                .map(post -> {
                    Category category = categories.get(post.getCategoryId());
                    SysUser author = users.get(post.getUserId());
                    return new AdminPostResponse(
                            post.getId(),
                            post.getTitle(),
                            post.getPostType(),
                            categoryName(category),
                            categoryCode(category),
                            authorNickname(author),
                            post.getStatus(),
                            isEnabled(post.getRecommended()),
                            isEnabled(post.getPinned()),
                            valueOrZero(post.getViewCount()),
                            valueOrZero(post.getLikeCount()),
                            valueOrZero(post.getCommentCount()),
                            post.getCreatedAt(),
                            post.getUpdatedAt()
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

    private static String categoryName(Category category) {
        return category == null ? "未知分区" : category.getName();
    }

    private static String categoryCode(Category category) {
        return category == null ? "unknown" : category.getCode();
    }

    private static String authorNickname(SysUser author) {
        return author == null ? "未知玩家" : author.getNickname();
    }

    private static boolean isEnabled(Integer value) {
        return value != null && value == 1;
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
