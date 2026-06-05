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
import com.tarkovcommunity.notification.entity.Notification;
import com.tarkovcommunity.notification.mapper.NotificationMapper;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class ForumCommentServiceImpl implements ForumCommentService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int UNREAD = 0;
    private static final int NOTIFICATION_CONTENT_MAX_LENGTH = 500;
    private static final String POST_COMMENT_NOTIFICATION_TYPE = "POST_COMMENT";
    private static final String COMMENT_REPLY_NOTIFICATION_TYPE = "COMMENT_REPLY";

    private final PostCommentMapper postCommentMapper;
    private final PostMapper postMapper;
    private final SysUserMapper sysUserMapper;
    private final NotificationMapper notificationMapper;

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
    @Transactional
    public CommentCreatedResponse createComment(Long postId, CommentCreateRequest request, SysUser author) {
        Post post = requireNormalPost(postId);

        if (request.replyToUserId() != null && sysUserMapper.selectById(request.replyToUserId()) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "回复用户不存在");
        }

        PostComment parent = null;
        if (request.parentId() != null) {
            parent = postCommentMapper.selectById(request.parentId());
            if (parent == null || !Objects.equals(parent.getPostId(), postId) || !"NORMAL".equals(parent.getStatus())) {
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
        createInteractionNotification(post, parent, request, author);

        return new CommentCreatedResponse(comment.getId());
    }

    private void createInteractionNotification(Post post, PostComment parent, CommentCreateRequest request, SysUser author) {
        Long recipientId;
        String type;
        String title;
        String prefix;

        if (parent == null) {
            recipientId = post.getUserId();
            type = POST_COMMENT_NOTIFICATION_TYPE;
            title = "帖子收到新评论";
            prefix = "你的帖子收到了新评论：";
        } else {
            recipientId = request.replyToUserId() == null ? parent.getUserId() : request.replyToUserId();
            type = COMMENT_REPLY_NOTIFICATION_TYPE;
            title = "评论收到新回复";
            prefix = "你的评论收到了新回复：";
        }

        if (recipientId == null || Objects.equals(recipientId, author.getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(recipientId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(limit(prefix + request.content(), NOTIFICATION_CONTENT_MAX_LENGTH));
        notification.setRelatedId(post.getId());
        notification.setReadStatus(UNREAD);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
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

    private static String limit(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
