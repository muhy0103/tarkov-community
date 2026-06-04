package com.tarkovcommunity.forum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.forum.controller.ForumReactionController;
import com.tarkovcommunity.forum.dto.PostActionRequest;
import com.tarkovcommunity.forum.dto.PostActionResponse;
import com.tarkovcommunity.forum.service.ForumReactionService;
import com.tarkovcommunity.user.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ForumReactionController.class)
class ForumReactionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ForumReactionService forumReactionService;

    @MockitoBean
    private AuthTokenService authTokenService;

    @Test
    void togglesPostLike() throws Exception {
        PostActionRequest request = new PostActionRequest(2L);

        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(normalUser()));
        given(forumReactionService.toggleLike(9L, 7L))
                .willReturn(new PostActionResponse(9L, 7L, true, 6));

        mockMvc.perform(post("/api/posts/9/likes/toggle")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.count").value(6));
    }

    @Test
    void togglesPostFavorite() throws Exception {
        PostActionRequest request = new PostActionRequest(2L);

        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(normalUser()));
        given(forumReactionService.toggleFavorite(9L, 7L))
                .willReturn(new PostActionResponse(9L, 7L, false, 1));

        mockMvc.perform(post("/api/posts/9/favorites/toggle")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.active").value(false))
                .andExpect(jsonPath("$.data.count").value(1));
    }

    @Test
    void rejectsReactionWithoutLogin() throws Exception {
        mockMvc.perform(post("/api/posts/9/likes/toggle")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    private static SysUser normalUser() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setUsername("pmc_rookie");
        user.setRole("USER");
        user.setStatus("NORMAL");
        return user;
    }
}
