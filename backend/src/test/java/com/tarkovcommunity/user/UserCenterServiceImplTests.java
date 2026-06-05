package com.tarkovcommunity.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.forum.entity.Favorite;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostComment;
import com.tarkovcommunity.forum.mapper.FavoriteMapper;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.meta.entity.Category;
import com.tarkovcommunity.meta.mapper.CategoryMapper;
import com.tarkovcommunity.user.dto.UserCenterSummaryResponse;
import com.tarkovcommunity.user.dto.UserPasswordUpdateRequest;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import com.tarkovcommunity.user.service.impl.UserCenterServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCenterServiceImplTests {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostCommentMapper postCommentMapper;

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Test
    void changesCurrentUserPassword() {
        UserCenterServiceImpl service = service();
        SysUser user = userWithPassword("OldPass123");

        service.updatePassword(user, new UserPasswordUpdateRequest("OldPass123", "NewPass123"));

        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).updateById(userCaptor.capture());
        SysUser updatedUser = userCaptor.getValue();

        assertThat(updatedUser.getId()).isEqualTo(7L);
        assertThat(passwordEncoder.matches("NewPass123", updatedUser.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("OldPass123", updatedUser.getPassword())).isFalse();
        assertThat(updatedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void rejectsWrongCurrentPassword() {
        UserCenterServiceImpl service = service();
        SysUser user = userWithPassword("OldPass123");

        assertThatThrownBy(() -> service.updatePassword(user, new UserPasswordUpdateRequest("WrongPass123", "NewPass123")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("当前密码不正确");

        verify(sysUserMapper, never()).updateById(user);
    }

    @Test
    void countsAndListsOnlyVisibleComments() {
        UserCenterServiceImpl service = service();
        SysUser user = userWithPassword("OldPass123");
        Page<PostComment> commentPage = new Page<>(1, 10, 1);
        commentPage.setRecords(List.of(comment(21L, 31L)));

        given(postMapper.selectCount(any())).willReturn(2L);
        given(postCommentMapper.selectVisiblePostCommentCount(7L)).willReturn(5L);
        given(favoriteMapper.selectVisiblePostFavoriteCount(7L)).willReturn(1L);
        given(postCommentMapper.selectVisiblePostCommentsPage(any(Page.class), eq(7L))).willReturn(commentPage);
        given(postMapper.selectBatchIds(any())).willReturn(List.of(post(31L)));

        UserCenterSummaryResponse summary = service.getSummary(user);
        var comments = service.listComments(user, 1, 10);

        assertThat(summary.commentCount()).isEqualTo(5L);
        assertThat(comments.total()).isEqualTo(1L);
        assertThat(comments.records()).hasSize(1);
        assertThat(comments.records().get(0).id()).isEqualTo(21L);
        assertThat(comments.records().get(0).postId()).isEqualTo(31L);
        assertThat(comments.records().get(0).postTitle()).isEqualTo("Visible favorite post");
        verify(postCommentMapper).selectVisiblePostCommentCount(7L);
        verify(postCommentMapper).selectVisiblePostCommentsPage(any(Page.class), eq(7L));
    }

    @Test
    void countsAndListsOnlyVisibleFavoritePosts() {
        UserCenterServiceImpl service = service();
        SysUser user = userWithPassword("OldPass123");
        Page<Favorite> favoritePage = new Page<>(1, 10, 1);
        favoritePage.setRecords(List.of(favorite(11L, 31L)));

        given(postMapper.selectCount(any())).willReturn(0L);
        given(postCommentMapper.selectVisiblePostCommentCount(7L)).willReturn(0L);
        given(favoriteMapper.selectVisiblePostFavoriteCount(7L)).willReturn(1L);
        given(favoriteMapper.selectVisiblePostFavoritesPage(any(Page.class), eq(7L))).willReturn(favoritePage);
        given(postMapper.selectBatchIds(any())).willReturn(List.of(post(31L)));
        given(categoryMapper.selectBatchIds(any())).willReturn(List.of(category()));
        given(sysUserMapper.selectBatchIds(any())).willReturn(List.of(user));

        UserCenterSummaryResponse summary = service.getSummary(user);
        var favorites = service.listFavorites(user, 1, 10);

        assertThat(summary.favoriteCount()).isEqualTo(1L);
        assertThat(favorites.total()).isEqualTo(1L);
        assertThat(favorites.records()).hasSize(1);
        assertThat(favorites.records().get(0).id()).isEqualTo(31L);
        assertThat(favorites.records().get(0).title()).isEqualTo("Visible favorite post");
        verify(favoriteMapper).selectVisiblePostFavoriteCount(7L);
        verify(favoriteMapper).selectVisiblePostFavoritesPage(any(Page.class), eq(7L));
    }

    private UserCenterServiceImpl service() {
        return new UserCenterServiceImpl(
                postMapper,
                postCommentMapper,
                favoriteMapper,
                categoryMapper,
                sysUserMapper
        );
    }

    private SysUser userWithPassword(String rawPassword) {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setUsername("pmc_rookie");
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname("Rookie");
        user.setRole("USER");
        user.setStatus("NORMAL");
        user.setContribution(18);
        return user;
    }

    private Favorite favorite(Long id, Long postId) {
        Favorite favorite = new Favorite();
        favorite.setId(id);
        favorite.setPostId(postId);
        favorite.setUserId(7L);
        favorite.setCreatedAt(LocalDateTime.now());
        return favorite;
    }

    private PostComment comment(Long id, Long postId) {
        PostComment comment = new PostComment();
        comment.setId(id);
        comment.setPostId(postId);
        comment.setUserId(7L);
        comment.setContent("Visible comment");
        comment.setStatus("NORMAL");
        comment.setLikeCount(3);
        comment.setCreatedAt(LocalDateTime.now());
        return comment;
    }

    private Post post(Long id) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(7L);
        post.setCategoryId(3L);
        post.setTitle("Visible favorite post");
        post.setContent("A favorite post that is still visible.");
        post.setPostType("DISCUSSION");
        post.setStatus("NORMAL");
        post.setViewCount(9);
        post.setLikeCount(2);
        post.setCommentCount(1);
        post.setRecommended(0);
        post.setCreatedAt(LocalDateTime.now());
        return post;
    }

    private Category category() {
        Category category = new Category();
        category.setId(3L);
        category.setName("Raid Stories");
        category.setCode("raid-stories");
        return category;
    }

}
