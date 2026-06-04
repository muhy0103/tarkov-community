package com.tarkovcommunity.forum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.controller.ForumCommentController;
import com.tarkovcommunity.forum.dto.CommentCreateRequest;
import com.tarkovcommunity.forum.dto.CommentCreatedResponse;
import com.tarkovcommunity.forum.dto.CommentResponse;
import com.tarkovcommunity.forum.service.ForumCommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ForumCommentController.class)
class ForumCommentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ForumCommentService forumCommentService;

    @Test
    void listsCommentsByPost() throws Exception {
        CommentResponse comment = new CommentResponse(
                1L,
                9L,
                2L,
                "夜莺",
                null,
                null,
                "这条路线适合单人做任务，建议带耳机听脚步。",
                3,
                LocalDateTime.of(2026, 6, 4, 19, 10)
        );

        given(forumCommentService.listComments(9L, 1, 20))
                .willReturn(PageResponse.of(1, 20, 1, List.of(comment)));

        mockMvc.perform(get("/api/posts/9/comments")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].authorNickname").value("夜莺"));
    }

    @Test
    void createsComment() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest(
                2L,
                "补充一个细节：如果出生点靠近伐木场，先观察枪声再决定路线。",
                null,
                null
        );

        given(forumCommentService.createComment(eq(9L), any(CommentCreateRequest.class)))
                .willReturn(new CommentCreatedResponse(12L));

        mockMvc.perform(post("/api/posts/9/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(12L));
    }

    @Test
    void rejectsInvalidCommentRequest() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest(null, "短", null, null);

        mockMvc.perform(post("/api/posts/9/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
