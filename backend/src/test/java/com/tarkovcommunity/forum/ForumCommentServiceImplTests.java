package com.tarkovcommunity.forum;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.CommentCreateRequest;
import com.tarkovcommunity.forum.dto.CommentResponse;
import com.tarkovcommunity.forum.entity.CommentLike;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostComment;
import com.tarkovcommunity.forum.mapper.CommentLikeMapper;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.forum.service.impl.ForumCommentServiceImpl;
import com.tarkovcommunity.notification.entity.Notification;
import com.tarkovcommunity.notification.mapper.NotificationMapper;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ForumCommentServiceImplTests {

    @Mock
    private PostCommentMapper postCommentMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private CommentLikeMapper commentLikeMapper;

    @Test
    void marksCommentsLikedByCurrentUser() {
        ForumCommentServiceImpl service = service();
        Page<PostComment> page = new Page<>(1, 20);
        page.setTotal(1);
        page.setRecords(List.of(normalComment()));
        given(postMapper.selectById(9L)).willReturn(normalPost());
        given(postCommentMapper.selectPage(any(), any())).willReturn(page);
        given(sysUserMapper.selectBatchIds(any())).willReturn(List.of(normalUser()));
        given(commentLikeMapper.selectList(any())).willReturn(List.of(existingCommentLike()));

        PageResponse<CommentResponse> response = service.listComments(9L, 1, 20, normalUser());

        assertThat(response.records()).hasSize(1);
        assertThat(response.records().get(0).likedByCurrentUser()).isTrue();
    }

    @Test
    void notifiesPostAuthorWhenNewCommentCreatedByOtherUser() {
        ForumCommentServiceImpl service = service();
        given(postMapper.selectById(9L)).willReturn(normalPost());

        service.createComment(9L, new CommentCreateRequest(
                null,
                "This route helped my squad get through Customs safely.",
                null,
                null
        ), normalUser());

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationMapper).insert(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertThat(notification.getUserId()).isEqualTo(8L);
        assertThat(notification.getType()).isEqualTo("POST_COMMENT");
        assertThat(notification.getRelatedId()).isEqualTo(9L);
        assertThat(notification.getReadStatus()).isEqualTo(0);
        assertThat(notification.getTitle()).contains("评论");
        assertThat(notification.getContent()).contains("Customs");
    }

    @Test
    void notifiesReplyTargetWhenReplyCreatedByOtherUser() {
        ForumCommentServiceImpl service = service();
        given(postMapper.selectById(9L)).willReturn(normalPost());
        given(sysUserMapper.selectById(11L)).willReturn(replyTargetUser());
        given(postCommentMapper.selectById(21L)).willReturn(parentComment("NORMAL"));

        service.createComment(9L, new CommentCreateRequest(
                null,
                "I tried the same route and the timing works.",
                21L,
                11L
        ), normalUser());

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationMapper).insert(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertThat(notification.getUserId()).isEqualTo(11L);
        assertThat(notification.getType()).isEqualTo("COMMENT_REPLY");
        assertThat(notification.getRelatedId()).isEqualTo(9L);
        assertThat(notification.getReadStatus()).isEqualTo(0);
        assertThat(notification.getTitle()).contains("回复");
    }

    @Test
    void skipsNotificationWhenCommentingOwnPost() {
        ForumCommentServiceImpl service = service();
        Post post = normalPost();
        post.setUserId(7L);
        given(postMapper.selectById(9L)).willReturn(post);

        service.createComment(9L, new CommentCreateRequest(
                null,
                "Adding a small update to my own route guide.",
                null,
                null
        ), normalUser());

        verify(notificationMapper, never()).insert(any(Notification.class));
    }

    @Test
    void rejectsReplyToHiddenParentComment() {
        ForumCommentServiceImpl service = service();
        given(postMapper.selectById(9L)).willReturn(normalPost());
        given(postCommentMapper.selectById(21L)).willReturn(parentComment("HIDDEN"));

        CommentCreateRequest request = new CommentCreateRequest(
                null,
                "Reply should not be attached to a hidden comment.",
                21L,
                null
        );

        assertThatThrownBy(() -> service.createComment(9L, request, normalUser()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
        verify(postCommentMapper, never()).insert(any(PostComment.class));
        verify(postMapper, never()).updateById(any(Post.class));
        verify(notificationMapper, never()).insert(any(Notification.class));
    }

    private ForumCommentServiceImpl service() {
        return new ForumCommentServiceImpl(postCommentMapper, postMapper, sysUserMapper, notificationMapper, commentLikeMapper);
    }

    private Post normalPost() {
        Post post = new Post();
        post.setId(9L);
        post.setUserId(8L);
        post.setTitle("Customs safe route");
        post.setStatus("NORMAL");
        post.setCommentCount(3);
        return post;
    }

    private PostComment parentComment(String status) {
        PostComment comment = new PostComment();
        comment.setId(21L);
        comment.setPostId(9L);
        comment.setUserId(8L);
        comment.setStatus(status);
        return comment;
    }

    private PostComment normalComment() {
        PostComment comment = new PostComment();
        comment.setId(21L);
        comment.setPostId(9L);
        comment.setUserId(7L);
        comment.setContent("Detailed route note.");
        comment.setLikeCount(2);
        comment.setStatus("NORMAL");
        return comment;
    }

    private CommentLike existingCommentLike() {
        CommentLike like = new CommentLike();
        like.setId(31L);
        like.setCommentId(21L);
        like.setUserId(7L);
        return like;
    }

    private SysUser replyTargetUser() {
        SysUser user = new SysUser();
        user.setId(11L);
        user.setUsername("reply_target");
        user.setRole("USER");
        user.setStatus("NORMAL");
        return user;
    }

    private SysUser normalUser() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setUsername("pmc_rookie");
        user.setRole("USER");
        user.setStatus("NORMAL");
        return user;
    }
}
