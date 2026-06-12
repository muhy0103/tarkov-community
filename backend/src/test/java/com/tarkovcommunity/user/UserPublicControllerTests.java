package com.tarkovcommunity.user;

import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.user.controller.UserPublicController;
import com.tarkovcommunity.user.dto.PublicUserProfileResponse;
import com.tarkovcommunity.user.service.UserPublicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserPublicController.class)
class UserPublicControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserPublicService userPublicService;

    @Test
    void getsPublicUserProfileWithoutLogin() throws Exception {
        given(userPublicService.getProfile(eq(7L))).willReturn(new PublicUserProfileResponse(
                7L,
                "pmc_rookie",
                "Rookie",
                "https://example.com/avatar.png",
                "USER",
                18,
                3L,
                5L,
                LocalDateTime.of(2026, 6, 4, 21, 30)
        ));

        mockMvc.perform(get("/api/users/7/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.nickname").value("Rookie"))
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.postCount").value(3))
                .andExpect(jsonPath("$.data.commentCount").value(5));
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
}
