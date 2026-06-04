package com.tarkovcommunity.forum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.forum.controller.ForumReactionController;
import com.tarkovcommunity.forum.dto.PostActionResponse;
import com.tarkovcommunity.forum.dto.PostActionRequest;
import com.tarkovcommunity.forum.service.ForumReactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    void togglesPostLike() throws Exception {
        PostActionRequest request = new PostActionRequest(2L);

        given(forumReactionService.toggleLike(9L, 2L))
                .willReturn(new PostActionResponse(9L, 2L, true, 6));

        mockMvc.perform(post("/api/posts/9/likes/toggle")
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

        given(forumReactionService.toggleFavorite(9L, 2L))
                .willReturn(new PostActionResponse(9L, 2L, false, 1));

        mockMvc.perform(post("/api/posts/9/favorites/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.active").value(false))
                .andExpect(jsonPath("$.data.count").value(1));
    }

    @Test
    void rejectsReactionWithoutUser() throws Exception {
        PostActionRequest request = new PostActionRequest(null);

        mockMvc.perform(post("/api/posts/9/likes/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
