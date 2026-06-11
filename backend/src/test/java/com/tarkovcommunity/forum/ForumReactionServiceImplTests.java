package com.tarkovcommunity.forum;

import com.tarkovcommunity.forum.dto.CommentActionResponse;
import com.tarkovcommunity.forum.dto.PostActionResponse;
import com.tarkovcommunity.forum.entity.CommentLike;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostComment;
import com.tarkovcommunity.forum.entity.PostLike;
import com.tarkovcommunity.forum.mapper.CommentLikeMapper;
import com.tarkovcommunity.forum.mapper.FavoriteMapper;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostLikeMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.forum.service.impl.ForumReactionServiceImpl;
import com.tarkovcommunity.notification.entity.Notification;
import com.tarkovcommunity.notification.mapper.NotificationMapper;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ForumReactionServiceImplTests {

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostLikeMapper postLikeMapper;

    @Mock
    private CommentLikeMapper commentLikeMapper;

    @Mock
    private PostCommentMapper postCommentMapper;

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private NotificationMapper notificationMapper;

    @Test
    void notifiesPostAuthorWhenLikedByOtherUser() {
        ForumReactionServiceImpl service = service();
        given(postMapper.selectById(9L)).willReturn(normalPost());
        given(sysUserMapper.selectById(7L)).willReturn(normalUser(7L));
        given(postLikeMapper.selectOne(any())).willReturn(null);

        PostActionResponse response = service.toggleLike(9L, 7L);

        assertThat(response.active()).isTrue();
        assertThat(response.count()).isEqualTo(3);
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationMapper).insert(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertThat(notification.getUserId()).isEqualTo(8L);
        assertThat(notification.getType()).isEqualTo("POST_LIKE");
        assertThat(notification.getRelatedId()).isEqualTo(9L);
        assertThat(notification.getReadStatus()).isEqualTo(0);
        assertThat(notification.getTitle()).contains("点赞");
    }

    @Test
    void skipsNotificationWhenLikingOwnPost() {
        ForumReactionServiceImpl service = service();
        Post post = normalPost();
        post.setUserId(7L);
        given(postMapper.selectById(9L)).willReturn(post);
        given(sysUserMapper.selectById(7L)).willReturn(normalUser(7L));
        given(postLikeMapper.selectOne(any())).willReturn(null);

        service.toggleLike(9L, 7L);

        verify(notificationMapper, never()).insert(any(Notification.class));
    }

    @Test
    void skipsNotificationWhenLikeIsRemoved() {
        ForumReactionServiceImpl service = service();
        given(postMapper.selectById(9L)).willReturn(normalPost());
        given(sysUserMapper.selectById(7L)).willReturn(normalUser(7L));
        given(postLikeMapper.selectOne(any())).willReturn(existingLike());

        PostActionResponse response = service.toggleLike(9L, 7L);

        assertThat(response.active()).isFalse();
        assertThat(response.count()).isEqualTo(1);
        verify(notificationMapper, never()).insert(any(Notification.class));
    }

    @Test
    void togglesCommentLikeOn() {
        ForumReactionServiceImpl service = service();
        given(postMapper.selectById(9L)).willReturn(normalPost());
        given(sysUserMapper.selectById(7L)).willReturn(normalUser(7L));
        given(postCommentMapper.selectById(21L)).willReturn(normalComment());
        given(commentLikeMapper.selectOne(any())).willReturn(null);

        CommentActionResponse response = service.toggleCommentLike(9L, 21L, 7L);

        assertThat(response.commentId()).isEqualTo(21L);
        assertThat(response.active()).isTrue();
        assertThat(response.count()).isEqualTo(5);
        verify(commentLikeMapper).insert(any(CommentLike.class));
        ArgumentCaptor<PostComment> commentCaptor = ArgumentCaptor.forClass(PostComment.class);
        verify(postCommentMapper).updateById(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getLikeCount()).isEqualTo(5);
    }

    @Test
    void togglesCommentLikeOff() {
        ForumReactionServiceImpl service = service();
        given(postMapper.selectById(9L)).willReturn(normalPost());
        given(sysUserMapper.selectById(7L)).willReturn(normalUser(7L));
        given(postCommentMapper.selectById(21L)).willReturn(normalComment());
        given(commentLikeMapper.selectOne(any())).willReturn(existingCommentLike());

        CommentActionResponse response = service.toggleCommentLike(9L, 21L, 7L);

        assertThat(response.active()).isFalse();
        assertThat(response.count()).isEqualTo(3);
        verify(commentLikeMapper).deleteById(31L);
        ArgumentCaptor<PostComment> commentCaptor = ArgumentCaptor.forClass(PostComment.class);
        verify(postCommentMapper).updateById(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getLikeCount()).isEqualTo(3);
    }

    private ForumReactionServiceImpl service() {
        return new ForumReactionServiceImpl(
                postMapper,
                postLikeMapper,
                commentLikeMapper,
                postCommentMapper,
                favoriteMapper,
                sysUserMapper,
                notificationMapper
        );
    }

    private Post normalPost() {
        Post post = new Post();
        post.setId(9L);
        post.setUserId(8L);
        post.setTitle("Customs timing guide");
        post.setStatus("NORMAL");
        post.setLikeCount(2);
        post.setFavoriteCount(1);
        return post;
    }

    private PostLike existingLike() {
        PostLike like = new PostLike();
        like.setId(21L);
        like.setPostId(9L);
        like.setUserId(7L);
        return like;
    }

    private PostComment normalComment() {
        PostComment comment = new PostComment();
        comment.setId(21L);
        comment.setPostId(9L);
        comment.setUserId(8L);
        comment.setContent("Good route");
        comment.setStatus("NORMAL");
        comment.setLikeCount(4);
        return comment;
    }

    private CommentLike existingCommentLike() {
        CommentLike like = new CommentLike();
        like.setId(31L);
        like.setCommentId(21L);
        like.setUserId(7L);
        return like;
    }

    private SysUser normalUser(Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername("pmc_" + id);
        user.setRole("USER");
        user.setStatus("NORMAL");
        return user;
    }
}
