package com.tarkovcommunity.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.mapper.PostCommentMapper;
import com.tarkovcommunity.forum.mapper.PostMapper;
import com.tarkovcommunity.meta.entity.Category;
import com.tarkovcommunity.meta.mapper.CategoryMapper;
import com.tarkovcommunity.user.entity.UserFollow;
import com.tarkovcommunity.user.entity.UserProfile;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.mapper.SysUserMapper;
import com.tarkovcommunity.user.mapper.UserFollowMapper;
import com.tarkovcommunity.user.mapper.UserProfileMapper;
import com.tarkovcommunity.user.service.impl.UserPublicServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserPublicServiceImplTests {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostCommentMapper postCommentMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private UserFollowMapper userFollowMapper;

    @Test
    void returnsPublicProfileWithoutPrivateEmail() {
        UserPublicServiceImpl service = service();
        SysUser user = normalUser();

        given(sysUserMapper.selectById(7L)).willReturn(user);
        given(postMapper.selectCount(any())).willReturn(3L);
        given(postCommentMapper.selectVisiblePostCommentCount(7L)).willReturn(5L);
        given(userProfileMapper.selectOne(any())).willReturn(profile());
        given(userFollowMapper.selectCount(any())).willReturn(4L, 2L);

        var profile = service.getProfile(7L, null);

        assertThat(profile.id()).isEqualTo(7L);
        assertThat(profile.username()).isEqualTo("pmc_rookie");
        assertThat(profile.nickname()).isEqualTo("Rookie");
        assertThat(profile.contribution()).isEqualTo(18);
        assertThat(profile.postCount()).isEqualTo(3L);
        assertThat(profile.commentCount()).isEqualTo(5L);
        assertThat(profile.bio()).isEqualTo("Runs Customs and Factory routes.");
        assertThat(profile.favoriteMaps()).isEqualTo("Customs,Factory");
        assertThat(profile.followerCount()).isEqualTo(4L);
        assertThat(profile.followingCount()).isEqualTo(2L);
        assertThat(profile.followedByMe()).isFalse();
        assertThat(profile.ownProfile()).isFalse();
    }

    @Test
    void returnsFollowStateForViewer() {
        UserPublicServiceImpl service = service();
        SysUser user = normalUser();
        SysUser viewer = normalUser();
        viewer.setId(3L);

        given(sysUserMapper.selectById(7L)).willReturn(user);
        given(postMapper.selectCount(any())).willReturn(3L);
        given(postCommentMapper.selectVisiblePostCommentCount(7L)).willReturn(5L);
        given(userFollowMapper.selectCount(any())).willReturn(4L, 2L, 1L);

        var profile = service.getProfile(7L, viewer);

        assertThat(profile.followedByMe()).isTrue();
        assertThat(profile.ownProfile()).isFalse();
    }

    @Test
    void rejectsDisabledPublicProfile() {
        UserPublicServiceImpl service = service();
        SysUser disabled = normalUser();
        disabled.setStatus("DISABLED");

        given(sysUserMapper.selectById(7L)).willReturn(disabled);

        assertThatThrownBy(() -> service.getProfile(7L, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("玩家不存在");
    }

    @Test
    void followsNormalUser() {
        UserPublicServiceImpl service = service();
        SysUser target = normalUser();
        SysUser viewer = normalUser();
        viewer.setId(3L);

        given(sysUserMapper.selectById(7L)).willReturn(target);
        given(userFollowMapper.selectCount(any())).willReturn(0L, 5L, 3L);

        var result = service.followUser(7L, viewer);

        verify(userFollowMapper).insert(argThat((UserFollow follow) ->
                follow.getUserId().equals(3L) && follow.getFollowedUserId().equals(7L)
        ));
        assertThat(result.followed()).isTrue();
        assertThat(result.followerCount()).isEqualTo(5L);
        assertThat(result.followingCount()).isEqualTo(3L);
    }

    @Test
    void rejectsFollowingSelf() {
        UserPublicServiceImpl service = service();
        SysUser viewer = normalUser();

        assertThatThrownBy(() -> service.followUser(7L, viewer))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("不能关注自己");
    }

    @Test
    void listsOnlyNormalPublicPosts() {
        UserPublicServiceImpl service = service();
        SysUser user = normalUser();
        Page<Post> postPage = new Page<>(1, 6, 1);
        postPage.setRecords(List.of(post(21L)));

        given(sysUserMapper.selectById(7L)).willReturn(user);
        given(postMapper.selectPage(any(Page.class), any())).willReturn(postPage);
        given(categoryMapper.selectBatchIds(any())).willReturn(List.of(category()));
        given(sysUserMapper.selectBatchIds(any())).willReturn(List.of(user));

        var posts = service.listPosts(7L, 1, 6);

        assertThat(posts.total()).isEqualTo(1L);
        assertThat(posts.records()).hasSize(1);
        assertThat(posts.records().get(0).id()).isEqualTo(21L);
        assertThat(posts.records().get(0).title()).isEqualTo("Factory budget loadout");
        assertThat(posts.records().get(0).authorNickname()).isEqualTo("Rookie");
    }

    private UserPublicServiceImpl service() {
        return new UserPublicServiceImpl(
                sysUserMapper,
                postMapper,
                postCommentMapper,
                categoryMapper,
                userProfileMapper,
                userFollowMapper
        );
    }

    private static SysUser normalUser() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setUsername("pmc_rookie");
        user.setNickname("Rookie");
        user.setAvatar("https://example.com/avatar.png");
        user.setRole("USER");
        user.setStatus("NORMAL");
        user.setContribution(18);
        user.setCreatedAt(LocalDateTime.of(2026, 6, 4, 21, 30));
        return user;
    }

    private static Post post(Long id) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(7L);
        post.setCategoryId(3L);
        post.setTitle("Factory budget loadout");
        post.setContent("A lightweight kit for early wipe factory fights.");
        post.setPostType("LOADOUT");
        post.setStatus("NORMAL");
        post.setViewCount(12);
        post.setLikeCount(3);
        post.setCommentCount(1);
        post.setRecommended(0);
        post.setCreatedAt(LocalDateTime.of(2026, 6, 4, 21, 45));
        return post;
    }

    private static Category category() {
        Category category = new Category();
        category.setId(3L);
        category.setName("Loadouts");
        category.setCode("loadouts");
        return category;
    }

    private static UserProfile profile() {
        UserProfile profile = new UserProfile();
        profile.setUserId(7L);
        profile.setBio("Runs Customs and Factory routes.");
        profile.setFavoriteMaps("Customs,Factory");
        profile.setPlayStyle("任务优先");
        profile.setServerRegion("Asia");
        return profile;
    }
}
