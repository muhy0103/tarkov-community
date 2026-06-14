package com.tarkovcommunity.forum;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostCatalogRelationRequest;
import com.tarkovcommunity.forum.dto.PostCreateRequest;
import com.tarkovcommunity.forum.dto.PostDetailResponse;
import com.tarkovcommunity.forum.dto.PostCreatedResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.forum.dto.PostUpdateRequest;
import com.tarkovcommunity.forum.dto.RelatedCatalogResponse;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.mapper.FavoriteMapper;
import com.tarkovcommunity.forum.mapper.PostLikeMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.forum.service.PostCatalogRelationService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
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

    @Mock
    private PostCatalogRelationService postCatalogRelationService;

    @Test
    void createPostSavesCatalogRelations() {
        ForumPostServiceImpl service = service();
        SysUser author = owner();
        PostCreateRequest request = new PostCreateRequest(
                author.getId(),
                2L,
                "Customs route guide",
                "Use this route to cross Customs safely.",
                "GUIDE",
                null,
                List.of(new PostCatalogRelationRequest("MAP", 1L, "主要地图"))
        );
        given(categoryMapper.selectById(2L)).willReturn(enabledCategory(2L));
        given(postMapper.insert(any(Post.class))).willAnswer(invocation -> {
            Post insertedPost = invocation.getArgument(0);
            insertedPost.setId(10L);
            return 1;
        });

        PostCreatedResponse response = service.createPost(request, author);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).insert(postCaptor.capture());
        Post insertedPost = postCaptor.getValue();
        assertThat(insertedPost.getUserId()).isEqualTo(author.getId());
        assertThat(insertedPost.getCategoryId()).isEqualTo(2L);
        assertThat(response.id()).isEqualTo(10L);
        verify(postCatalogRelationService).replaceRelations(eq(10L), eq(request.relations()));
    }

    @Test
    void createPostPropagatesRelationFailureInsideTransaction() {
        ForumPostServiceImpl service = service();
        SysUser author = owner();
        PostCreateRequest request = new PostCreateRequest(
                author.getId(),
                2L,
                "Customs route guide",
                "Use this route to cross Customs safely.",
                "GUIDE",
                null,
                List.of(new PostCatalogRelationRequest("MAP", 1L, "main map"))
        );
        given(categoryMapper.selectById(2L)).willReturn(enabledCategory(2L));
        given(postMapper.insert(any(Post.class))).willAnswer(invocation -> {
            Post insertedPost = invocation.getArgument(0);
            insertedPost.setId(10L);
            return 1;
        });
        willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid relation"))
                .given(postCatalogRelationService)
                .replaceRelations(eq(10L), eq(request.relations()));

        assertThatThrownBy(() -> service.createPost(request, author))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        verify(postMapper).insert(any(Post.class));
    }

    @Test
    void postWriteMethodsAreTransactional() throws Exception {
        Method createPost = ForumPostServiceImpl.class.getMethod("createPost", PostCreateRequest.class, SysUser.class);
        Method updatePost = ForumPostServiceImpl.class.getMethod("updatePost", Long.class, PostUpdateRequest.class, SysUser.class);

        assertThat(createPost.getAnnotation(Transactional.class)).isNotNull();
        assertThat(updatePost.getAnnotation(Transactional.class)).isNotNull();
    }

    @Test
    void listPostsCanFilterByCatalogRelation() {
        ForumPostServiceImpl service = service();
        Post post = post();
        Page<Post> selectedPage = new Page<>(1, 10);
        selectedPage.setRecords(List.of(post));
        selectedPage.setTotal(1);
        RelatedCatalogResponse relation = new RelatedCatalogResponse(
                "MAP",
                1L,
                "Customs",
                "Normal",
                null,
                "maps",
                "主要地图"
        );
        given(postCatalogRelationService.selectRelatedPostsPage("MAP", 1L, 1, 10)).willReturn(selectedPage);
        given(categoryMapper.selectBatchIds(List.of(2L))).willReturn(List.of(enabledCategory(2L)));
        given(sysUserMapper.selectBatchIds(List.of(7L))).willReturn(List.of(owner()));
        given(postCatalogRelationService.findRelationsByPostIds(List.of(9L))).willReturn(Map.of(9L, List.of(relation)));

        PageResponse<PostSummaryResponse> response = service.listPosts(
                null,
                null,
                null,
                null,
                "LATEST",
                "MAP",
                1L,
                1,
                10
        );

        assertThat(response.records()).hasSize(1);
        assertThat(response.records().get(0).relations()).containsExactly(relation);
    }

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
                "  ",
                null
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
    void updatePostPropagatesRelationFailureInsideTransaction() {
        ForumPostServiceImpl service = service();
        List<PostCatalogRelationRequest> relations = List.of(new PostCatalogRelationRequest("MAP", 1L, "main map"));
        PostUpdateRequest updateRequest = new PostUpdateRequest(
                3L,
                "Customs route update",
                "This update keeps the route notes long enough for validation.",
                "GUIDE",
                null,
                relations
        );
        given(postMapper.selectById(9L)).willReturn(post());
        given(categoryMapper.selectById(3L)).willReturn(enabledCategory());
        willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid relation"))
                .given(postCatalogRelationService)
                .replaceRelations(eq(9L), eq(relations));

        assertThatThrownBy(() -> service.updatePost(9L, updateRequest, owner()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));

        verify(postMapper).updateById(any(Post.class));
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
        return new ForumPostServiceImpl(
                postMapper,
                categoryMapper,
                sysUserMapper,
                postLikeMapper,
                favoriteMapper,
                postCatalogRelationService
        );
    }

    private static PostUpdateRequest request() {
        return new PostUpdateRequest(
                3L,
                "海岸线任务路线更新版",
                "补充了疗养院外圈和发电站附近的绕行方案，适合低等级玩家参考。",
                "GUIDE",
                null,
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
        return enabledCategory(3L);
    }

    private static Category enabledCategory(Long id) {
        Category category = new Category();
        category.setId(id);
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
