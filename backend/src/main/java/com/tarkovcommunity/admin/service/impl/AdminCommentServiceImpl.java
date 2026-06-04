package com.tarkovcommunity.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.admin.dto.AdminCommentResponse;
import com.tarkovcommunity.admin.dto.AdminCommentReviewRequest;
import com.tarkovcommunity.admin.service.AdminCommentService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostComment;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
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
public class AdminCommentServiceImpl implements AdminCommentService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> ALLOWED_STATUSES = Set.of("NORMAL", "HIDDEN", "DELETED");

    private final PostCommentMapper commentMapper;
    private final PostMapper postMapper;
    private final SysUserMapper userMapper;

    @Override
    public PageResponse<AdminCommentResponse> listComments(
            String status,
            String keyword,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));

        LambdaQueryWrapper<PostComment> query = new LambdaQueryWrapper<PostComment>()
                .orderByDesc(PostComment::getCreatedAt)
                .orderByDesc(PostComment::getId);

        if (StringUtils.hasText(status)) {
            query.eq(PostComment::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            query.like(PostComment::getContent, keyword);
        }

        Page<PostComment> commentPage = commentMapper.selectPage(new Page<>(safePage, safeSize), query);
        return PageResponse.of(safePage, safeSize, commentPage.getTotal(), toResponses(commentPage.getRecords()));
    }

    @Override
    public AdminCommentResponse reviewComment(Long id, AdminCommentReviewRequest request) {
        PostComment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "评论不存在");
        }

        if (!StringUtils.hasText(request.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "评论状态不能为空");
        }

        if (!ALLOWED_STATUSES.contains(request.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "评论状态不正确");
        }

        String oldStatus = comment.getStatus();
        comment.setStatus(request.status());
        commentMapper.updateById(comment);
        adjustPostCommentCount(comment.getPostId(), oldStatus, request.status());

        return toResponses(List.of(commentMapper.selectById(id))).get(0);
    }

    private void adjustPostCommentCount(Long postId, String oldStatus, String newStatus) {
        if (Objects.equals(oldStatus, newStatus)) {
            return;
        }

        boolean wasVisible = "NORMAL".equals(oldStatus);
        boolean isVisible = "NORMAL".equals(newStatus);
        if (wasVisible == isVisible) {
            return;
        }

        Post post = postMapper.selectById(postId);
        if (post == null) {
            return;
        }

        int currentCount = valueOrZero(post.getCommentCount());
        post.setCommentCount(isVisible ? currentCount + 1 : Math.max(0, currentCount - 1));
        postMapper.updateById(post);
    }

    private List<AdminCommentResponse> toResponses(List<PostComment> comments) {
        if (comments.isEmpty()) {
            return List.of();
        }

        Map<Long, Post> posts = selectByIds(
                comments.stream().map(PostComment::getPostId).filter(Objects::nonNull).toList(),
                postMapper::selectBatchIds,
                Post::getId
        );
        Map<Long, SysUser> users = selectByIds(
                comments.stream().map(PostComment::getUserId).filter(Objects::nonNull).toList(),
                userMapper::selectBatchIds,
                SysUser::getId
        );

        return comments.stream()
                .map(comment -> {
                    Post post = posts.get(comment.getPostId());
                    SysUser author = users.get(comment.getUserId());
                    return new AdminCommentResponse(
                            comment.getId(),
                            comment.getPostId(),
                            postTitle(post),
                            authorNickname(author),
                            comment.getParentId(),
                            comment.getContent(),
                            comment.getStatus(),
                            valueOrZero(comment.getLikeCount()),
                            comment.getCreatedAt(),
                            comment.getUpdatedAt()
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

    private static String postTitle(Post post) {
        return post == null ? "未知帖子" : post.getTitle();
    }

    private static String authorNickname(SysUser author) {
        return author == null ? "未知玩家" : author.getNickname();
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
