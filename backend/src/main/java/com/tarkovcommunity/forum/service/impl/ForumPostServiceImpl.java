package com.tarkovcommunity.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostCreateRequest;
import com.tarkovcommunity.forum.dto.PostCreatedResponse;
import com.tarkovcommunity.forum.dto.PostDetailResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.forum.service.ForumPostService;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumPostServiceImpl implements ForumPostService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PostMapper postMapper;
    private final CategoryMapper categoryMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public PageResponse<PostSummaryResponse> listPosts(
            String categoryCode,
            String keyword,
            String postType,
            Boolean recommended,
            String sort,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<Post> query = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, "NORMAL");

        if (StringUtils.hasText(categoryCode)) {
            Category category = categoryMapper.selectOne(new LambdaQueryWrapper<Category>()
                    .eq(Category::getCode, categoryCode)
                    .eq(Category::getStatus, "ENABLED"));
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

        if (StringUtils.hasText(postType)) {
            query.eq(Post::getPostType, postType);
        }

        if (recommended != null) {
            query.eq(Post::getRecommended, recommended ? 1 : 0);
        }

        applySort(query, sort);

        Page<Post> postPage = postMapper.selectPage(new Page<>(safePage, safeSize), query);
        List<PostSummaryResponse> records = toSummaries(postPage.getRecords());
        return PageResponse.of(safePage, safeSize, postPage.getTotal(), records);
    }

    @Override
    public PostDetailResponse getPost(Long id) {
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, id)
                .eq(Post::getStatus, "NORMAL"));

        if (post == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "帖子不存在");
        }

        Category category = categoryMapper.selectById(post.getCategoryId());
        SysUser author = sysUserMapper.selectById(post.getUserId());

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
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
    }

    @Override
    public PostCreatedResponse createPost(PostCreateRequest request, SysUser author) {
        Category category = categoryMapper.selectById(request.categoryId());
        if (category == null || !"ENABLED".equals(category.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分区不存在或已停用");
        }

        Post post = new Post();
        post.setUserId(author.getId());
        post.setCategoryId(request.categoryId());
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setPostType(request.postType());
        post.setCoverImage(request.coverImage());
        post.setStatus("NORMAL");
        post.setPinned(0);
        post.setRecommended(0);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setFavoriteCount(0);
        post.setCommentCount(0);

        postMapper.insert(post);
        return new PostCreatedResponse(post.getId());
    }

    private List<PostSummaryResponse> toSummaries(List<Post> posts) {
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

    private static String excerpt(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        return normalized.length() > 90 ? normalized.substring(0, 90) + "..." : normalized;
    }

    private static void applySort(LambdaQueryWrapper<Post> query, String sort) {
        query.orderByDesc(Post::getPinned)
                .orderByDesc(Post::getRecommended);

        String normalized = StringUtils.hasText(sort) ? sort.trim().toUpperCase() : "LATEST";
        switch (normalized) {
            case "HOT" -> query.orderByDesc(Post::getViewCount)
                    .orderByDesc(Post::getLikeCount)
                    .orderByDesc(Post::getCommentCount)
                    .orderByDesc(Post::getCreatedAt);
            case "MOST_COMMENTED" -> query.orderByDesc(Post::getCommentCount)
                    .orderByDesc(Post::getLikeCount)
                    .orderByDesc(Post::getCreatedAt);
            case "MOST_LIKED" -> query.orderByDesc(Post::getLikeCount)
                    .orderByDesc(Post::getCommentCount)
                    .orderByDesc(Post::getCreatedAt);
            default -> query.orderByDesc(Post::getCreatedAt);
        }
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

    private static boolean isRecommended(Post post) {
        return post.getRecommended() != null && post.getRecommended() == 1;
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
