package com.tarkovcommunity.forum;

import com.tarkovcommunity.forum.dto.CommentCreateRequest;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostComment;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.forum.service.impl.ForumCommentServiceImpl;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        verify(postCommentMapper, never()).insert(org.mockito.ArgumentMatchers.any(PostComment.class));
        verify(postMapper, never()).updateById(org.mockito.ArgumentMatchers.any(Post.class));
    }

    private ForumCommentServiceImpl service() {
        return new ForumCommentServiceImpl(postCommentMapper, postMapper, sysUserMapper);
    }

    private Post normalPost() {
        Post post = new Post();
        post.setId(9L);
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

    private SysUser normalUser() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setUsername("pmc_rookie");
        user.setRole("USER");
        user.setStatus("NORMAL");
        return user;
    }
}
