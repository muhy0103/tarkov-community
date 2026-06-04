package com.tarkovcommunity.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.CommentCreateRequest;
import com.tarkovcommunity.forum.dto.CommentCreatedResponse;
import com.tarkovcommunity.forum.dto.CommentResponse;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostComment;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.forum.service.ForumCommentService;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumCommentServiceImpl implements ForumCommentService {

    private static final int MAX_PAGE_SIZE = 100;

    private final PostCommentMapper postCommentMapper;
    private final PostMapper postMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public PageResponse<CommentResponse> listComments(Long postId, int page, int size) {
        requireNormalPost(postId);

        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        Page<PostComment> commentPage = postCommentMapper.selectPage(
                new Page<>(safePage, safeSize),
                new LambdaQueryWrapper<PostComment>()
                        .eq(PostComment::getPostId, postId)
                        .eq(PostComment::getStatus, "NORMAL")
                        .orderByAsc(PostComment::getCreatedAt)
                        .orderByAsc(PostComment::getId)
        );

        return PageResponse.of(
                safePage,
                safeSize,
                commentPage.getTotal(),
                toResponses(commentPage.getRecords())
        );
    }

    @Override
    public CommentCreatedResponse createComment(Long postId, CommentCreateRequest request, SysUser author) {
        Post post = requireNormalPost(postId);

        if (request.replyToUserId() != null && sysUserMapper.selectById(request.replyToUserId()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "回复用户不存在");
        }

        if (request.parentId() != null) {
            PostComment parent = postCommentMapper.selectById(request.parentId());
            if (parent == null || !Objects.equals(parent.getPostId(), postId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "父评论不存在");
            }
        }

        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(author.getId());
        comment.setParentId(request.parentId());
        comment.setReplyToUserId(request.replyToUserId());
        comment.setContent(request.content());
        comment.setLikeCount(0);
        comment.setStatus("NORMAL");
        postCommentMapper.insert(comment);

        post.setCommentCount(valueOrZero(post.getCommentCount()) + 1);
        postMapper.updateById(post);

        return new CommentCreatedResponse(comment.getId());
    }

    private Post requireNormalPost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !"NORMAL".equals(post.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "帖子不存在");
        }
        return post;
    }

    private List<CommentResponse> toResponses(List<PostComment> comments) {
        if (comments.isEmpty()) {
            return List.of();
        }

        Map<Long, SysUser> users = selectUsers(comments.stream()
                .map(PostComment::getUserId)
                .filter(Objects::nonNull)
                .toList());

        return comments.stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getPostId(),
                        comment.getUserId(),
                        authorNickname(users.get(comment.getUserId())),
                        comment.getParentId(),
                        comment.getReplyToUserId(),
                        comment.getContent(),
                        valueOrZero(comment.getLikeCount()),
                        comment.getCreatedAt()
                ))
                .toList();
    }

    private Map<Long, SysUser> selectUsers(List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysUserMapper.selectBatchIds(ids.stream().distinct().toList())
                .stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity()));
    }

    private static String authorNickname(SysUser author) {
        return author == null ? "未知玩家" : author.getNickname();
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
