package com.tarkovcommunity.forum;

import com.tarkovcommunity.forum.dto.PostDetailResponse;
import com.tarkovcommunity.forum.dto.PostCreatedResponse;
import com.tarkovcommunity.forum.dto.PostUpdateRequest;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.mapper.FavoriteMapper;
import com.tarkovcommunity.forum.mapper.PostLikeMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.forum.service.impl.ForumPostServiceImpl;
import com.tarkovcommunity.meta.entity.Category;
import com.tarkovcommunity.meta.mapper.CategoryMapper;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ForumPostServiceImplTests {

    @Mock
    private PostMapper postMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private PostLikeMapper postLikeMapper;

    @Mock
    private FavoriteMapper favoriteMapper;

    @Test
    void incrementsViewCountWhenGettingDetail() {
        ForumPostServiceImpl service = service();
        Post post = post();
        post.setViewCount(12);
        post.setLikeCount(3);
        post.setCommentCount(2);
        given(postMapper.selectOne(any())).willReturn(post);
        given(categoryMapper.selectById(2L)).willReturn(enabledCategory());
        given(sysUserMapper.selectById(7L)).willReturn(owner());

        PostDetailResponse response = service.getPost(9L, null);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).updateById(postCaptor.capture());
        assertThat(postCaptor.getValue().getViewCount()).isEqualTo(13);
        assertThat(response.viewCount()).isEqualTo(13);
    }

    @Test
    void returnsViewerReactionStateWhenGettingDetail() {
        ForumPostServiceImpl service = service();
        Post post = post();
        post.setViewCount(12);
        post.setLikeCount(3);
        post.setFavoriteCount(5);
        post.setCommentCount(2);
        given(postMapper.selectOne(any())).willReturn(post);
        given(categoryMapper.selectById(2L)).willReturn(enabledCategory());
        given(sysUserMapper.selectById(7L)).willReturn(owner());
        given(postLikeMapper.selectCount(any())).willReturn(1L);
        given(favoriteMapper.selectCount(any())).willReturn(1L);

        PostDetailResponse response = service.getPost(9L, normalViewer());

        assertThat(response.favoriteCount()).isEqualTo(5);
        assertThat(response.likedByCurrentUser()).isTrue();
        assertThat(response.favoritedByCurrentUser()).isTrue();
    }

    @Test
    void updatesOwnPostAndNormalizesText() {
        ForumPostServiceImpl service = service();
        given(postMapper.selectById(9L)).willReturn(post());
        given(categoryMapper.selectById(3L)).willReturn(enabledCategory());

        PostCreatedResponse response = service.updatePost(9L, new PostUpdateRequest(
                3L,
                " 海岸线任务路线更新版 ",
                " 补充了疗养院外圈和发电站附近的绕行方案，适合低等级玩家参考。 ",
                " GUIDE ",
                "  "
        ), owner());

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).updateById(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getCategoryId()).isEqualTo(3L);
        assertThat(savedPost.getTitle()).isEqualTo("海岸线任务路线更新版");
        assertThat(savedPost.getContent()).isEqualTo("补充了疗养院外圈和发电站附近的绕行方案，适合低等级玩家参考。");
        assertThat(savedPost.getPostType()).isEqualTo("GUIDE");
        assertThat(savedPost.getCoverImage()).isNull();
        assertThat(response.id()).isEqualTo(9L);
    }

    @Test
    void rejectsEditingAnotherUsersPost() {
        ForumPostServiceImpl service = service();
        given(postMapper.selectById(9L)).willReturn(post());

        assertThatThrownBy(() -> service.updatePost(9L, request(), otherUser()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void rejectsDisabledCategoryOnUpdate() {
        ForumPostServiceImpl service = service();
        given(postMapper.selectById(9L)).willReturn(post());
        Category category = enabledCategory();
        category.setStatus("DISABLED");
        given(categoryMapper.selectById(3L)).willReturn(category);

        assertThatThrownBy(() -> service.updatePost(9L, request(), owner()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void withdrawsOwnPostByHidingIt() {
        ForumPostServiceImpl service = service();
        given(postMapper.selectById(9L)).willReturn(post());

        PostCreatedResponse response = service.withdrawPost(9L, owner());

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).updateById(postCaptor.capture());
        assertThat(postCaptor.getValue().getStatus()).isEqualTo("HIDDEN");
        assertThat(response.id()).isEqualTo(9L);
    }

    @Test
    void rejectsWithdrawingAnotherUsersPost() {
        ForumPostServiceImpl service = service();
        given(postMapper.selectById(9L)).willReturn(post());

        assertThatThrownBy(() -> service.withdrawPost(9L, otherUser()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    private ForumPostServiceImpl service() {
        return new ForumPostServiceImpl(postMapper, categoryMapper, sysUserMapper, postLikeMapper, favoriteMapper);
    }

    private static PostUpdateRequest request() {
        return new PostUpdateRequest(
                3L,
                "海岸线任务路线更新版",
                "补充了疗养院外圈和发电站附近的绕行方案，适合低等级玩家参考。",
                "GUIDE",
                null
        );
    }

    private static Post post() {
        Post post = new Post();
        post.setId(9L);
        post.setUserId(7L);
        post.setCategoryId(2L);
        post.setTitle("旧标题");
        post.setContent("旧正文内容");
        post.setPostType("ROUTE");
        post.setStatus("NORMAL");
        return post;
    }

    private static Category enabledCategory() {
        Category category = new Category();
        category.setId(3L);
        category.setName("任务攻略");
        category.setCode("quests");
        category.setStatus("ENABLED");
        return category;
    }

    private static SysUser owner() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setUsername("owner");
        user.setRole("USER");
        user.setStatus("NORMAL");
        return user;
    }

    private static SysUser otherUser() {
        SysUser user = new SysUser();
        user.setId(8L);
        user.setUsername("other");
        user.setRole("USER");
        user.setStatus("NORMAL");
        return user;
    }

    private static SysUser normalViewer() {
        SysUser user = new SysUser();
        user.setId(11L);
        user.setUsername("viewer");
        user.setRole("USER");
        user.setStatus("NORMAL");
        return user;
    }
}
