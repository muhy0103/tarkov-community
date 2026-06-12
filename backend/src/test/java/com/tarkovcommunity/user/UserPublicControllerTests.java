package com.tarkovcommunity.user;

import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.user.dto.FollowActionResponse;
import com.tarkovcommunity.user.controller.UserPublicController;
import com.tarkovcommunity.user.dto.PublicUserProfileResponse;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.service.UserPublicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserPublicController.class)
class UserPublicControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserPublicService userPublicService;

    @MockitoBean
    private AuthTokenService authTokenService;

    @Test
    void getsPublicUserProfileWithoutLogin() throws Exception {
        given(userPublicService.getProfile(eq(7L), any())).willReturn(publicProfile(false, false));

        mockMvc.perform(get("/api/users/7/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.nickname").value("Rookie"))
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.bio").value("Runs Customs and Factory routes."))
                .andExpect(jsonPath("$.data.favoriteMaps").value("Customs,Factory"))
                .andExpect(jsonPath("$.data.followerCount").value(4))
                .andExpect(jsonPath("$.data.followingCount").value(2))
                .andExpect(jsonPath("$.data.followedByMe").value(false))
                .andExpect(jsonPath("$.data.ownProfile").value(false));
    }

    @Test
    void getsPublicUserProfileWithLoginFollowState() throws Exception {
        SysUser viewer = user(3L);
        given(authTokenService.resolveUser("Bearer token")).willReturn(Optional.of(viewer));
        given(userPublicService.getProfile(eq(7L), eq(viewer))).willReturn(publicProfile(true, false));

        mockMvc.perform(get("/api/users/7/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followedByMe").value(true))
                .andExpect(jsonPath("$.data.ownProfile").value(false));
    }

    @Test
    void followsPublicUserWithLogin() throws Exception {
        SysUser viewer = user(3L);
        given(authTokenService.resolveUser("Bearer token")).willReturn(Optional.of(viewer));
        given(userPublicService.followUser(eq(7L), eq(viewer)))
                .willReturn(new FollowActionResponse(7L, 3L, true, 5L, 3L));

        mockMvc.perform(post("/api/users/7/follow")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followed").value(true))
                .andExpect(jsonPath("$.data.followerCount").value(5))
                .andExpect(jsonPath("$.data.followingCount").value(3));
    }

    @Test
    void unfollowsPublicUserWithLogin() throws Exception {
        SysUser viewer = user(3L);
        given(authTokenService.resolveUser("Bearer token")).willReturn(Optional.of(viewer));
        given(userPublicService.unfollowUser(eq(7L), eq(viewer)))
                .willReturn(new FollowActionResponse(7L, 3L, false, 4L, 2L));

        mockMvc.perform(delete("/api/users/7/follow")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.followed").value(false))
                .andExpect(jsonPath("$.data.followerCount").value(4))
                .andExpect(jsonPath("$.data.followingCount").value(2));
    }

    private static PublicUserProfileResponse publicProfile(boolean followedByMe, boolean ownProfile) {
        return new PublicUserProfileResponse(
                7L,
                "pmc_rookie",
                "Rookie",
                "https://example.com/avatar.png",
                "USER",
                18,
                3L,
                5L,
                "Runs Customs and Factory routes.",
                "Customs,Factory",
                "任务优先",
                "Asia",
                4L,
                2L,
                followedByMe,
                ownProfile,
                LocalDateTime.of(2026, 6, 4, 21, 30)
        );
    }

    @Test
    void listsPublicUserPostsWithoutLogin() throws Exception {
        given(userPublicService.listPosts(eq(7L), eq(1), eq(6)))
                .willReturn(PageResponse.of(1, 6, 1, List.of(postSummary())));

        mockMvc.perform(get("/api/users/7/posts")
                        .param("page", "1")
                        .param("size", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("Factory budget loadout"));
    }

    private static PostSummaryResponse postSummary() {
        return new PostSummaryResponse(
                21L,
                "Factory budget loadout",
                "A lightweight kit for early wipe factory fights.",
                "LOADOUT",
                "Loadouts",
                "loadouts",
                7L,
                "Rookie",
                false,
                12,
                3,
                1,
                LocalDateTime.of(2026, 6, 4, 21, 45)
        );
    }

    private static SysUser user(Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername("viewer");
        user.setNickname("Viewer");
        user.setStatus("NORMAL");
        return user;
    }
}
