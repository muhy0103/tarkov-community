package com.tarkovcommunity.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.user.controller.UserCenterController;
import com.tarkovcommunity.user.dto.UserCenterCommentResponse;
import com.tarkovcommunity.user.dto.UserCenterSummaryResponse;
import com.tarkovcommunity.user.dto.UserPasswordUpdateRequest;
import com.tarkovcommunity.user.dto.UserProfileUpdateRequest;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.service.UserCenterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserCenterController.class)
class UserCenterControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserCenterService userCenterService;

    @MockitoBean
    private AuthTokenService authTokenService;

    @Test
    void getsCurrentUserSummary() throws Exception {
        SysUser user = normalUser();
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(userCenterService.getSummary(eq(user))).willReturn(new UserCenterSummaryResponse(
                7L,
                "pmc_rookie",
                "Rookie",
                "rookie@example.com",
                null,
                "USER",
                "NORMAL",
                18,
                3L,
                5L,
                2L,
                LocalDateTime.of(2026, 6, 4, 21, 30)
        ));

        mockMvc.perform(get("/api/users/me/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("pmc_rookie"))
                .andExpect(jsonPath("$.data.postCount").value(3))
                .andExpect(jsonPath("$.data.commentCount").value(5))
                .andExpect(jsonPath("$.data.favoriteCount").value(2));
    }

    @Test
    void listsCurrentUserPosts() throws Exception {
        SysUser user = normalUser();
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(userCenterService.listPosts(eq(user), eq(1), eq(10)))
                .willReturn(PageResponse.of(1, 10, 1, List.of(postSummary())));

        mockMvc.perform(get("/api/users/me/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("Factory budget loadout"));
    }

    @Test
    void listsCurrentUserComments() throws Exception {
        SysUser user = normalUser();
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(userCenterService.listComments(eq(user), eq(1), eq(10)))
                .willReturn(PageResponse.of(1, 10, 1, List.of(new UserCenterCommentResponse(
                        11L,
                        21L,
                        "Customs dorms route",
                        "Keep a headset and painkillers ready.",
                        "NORMAL",
                        4,
                        LocalDateTime.of(2026, 6, 4, 22, 0)
                ))));

        mockMvc.perform(get("/api/users/me/comments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].postTitle").value("Customs dorms route"))
                .andExpect(jsonPath("$.data.records[0].content").value("Keep a headset and painkillers ready."));
    }

    @Test
    void updatesCurrentUserProfile() throws Exception {
        SysUser user = normalUser();
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(userCenterService.updateProfile(eq(user), any(UserProfileUpdateRequest.class)))
                .willReturn(new UserCenterSummaryResponse(
                        7L,
                        "pmc_rookie",
                        "Rookie Prime",
                        "prime@example.com",
                        "https://example.com/avatar.png",
                        "USER",
                        "NORMAL",
                        18,
                        3L,
                        5L,
                        2L,
                        LocalDateTime.of(2026, 6, 4, 21, 30)
                ));

        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                "Rookie Prime",
                "prime@example.com",
                "https://example.com/avatar.png"
        );

        mockMvc.perform(put("/api/users/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("Rookie Prime"))
                .andExpect(jsonPath("$.data.email").value("prime@example.com"))
                .andExpect(jsonPath("$.data.avatar").value("https://example.com/avatar.png"));
    }

    @Test
    void rejectsInvalidProfileUpdate() throws Exception {
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(normalUser()));

        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                "",
                "not-an-email",
                "https://example.com/avatar.png"
        );

        mockMvc.perform(put("/api/users/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void updatesCurrentUserPassword() throws Exception {
        SysUser user = normalUser();
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));

        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest(
                "OldPass123",
                "NewPass123"
        );

        mockMvc.perform(put("/api/users/me/password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void rejectsInvalidPasswordUpdate() throws Exception {
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(normalUser()));

        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest(
                "",
                "123"
        );

        mockMvc.perform(put("/api/users/me/password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void rejectsGuestAccess() throws Exception {
        given(authTokenService.resolveUser(eq(null))).willReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    private static SysUser normalUser() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setUsername("pmc_rookie");
        user.setNickname("Rookie");
        user.setEmail("rookie@example.com");
        user.setRole("USER");
        user.setStatus("NORMAL");
        user.setContribution(18);
        return user;
    }

    private static PostSummaryResponse postSummary() {
        return new PostSummaryResponse(
                21L,
                "Factory budget loadout",
                "A lightweight kit for early wipe factory fights.",
                "LOADOUT",
                "Loadouts",
                "loadouts",
                "Rookie",
                false,
                12,
                3,
                1,
                LocalDateTime.of(2026, 6, 4, 21, 45)
        );
    }
}
