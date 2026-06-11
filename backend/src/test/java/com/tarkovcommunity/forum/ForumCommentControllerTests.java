package com.tarkovcommunity.forum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.controller.ForumCommentController;
import com.tarkovcommunity.forum.dto.CommentCreateRequest;
import com.tarkovcommunity.forum.dto.CommentCreatedResponse;
import com.tarkovcommunity.forum.dto.CommentResponse;
import com.tarkovcommunity.forum.dto.CommentUpdateRequest;
import com.tarkovcommunity.forum.dto.CommentUpdatedResponse;
import com.tarkovcommunity.forum.dto.CommentWithdrawResponse;
import com.tarkovcommunity.forum.service.ForumCommentService;
import com.tarkovcommunity.user.entity.SysUser;
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
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @MockitoBean
    private AuthTokenService authTokenService;

    @Test
    void listsCommentsByPost() throws Exception {
        SysUser user = normalUser();
        CommentResponse comment = new CommentResponse(
                1L,
                9L,
                2L,
                "夜莺",
                null,
                null,
                "这条路线适合单人做任务，建议带耳机听脚步。",
                3,
                LocalDateTime.of(2026, 6, 4, 19, 10),
                true
        );

        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(forumCommentService.listComments(9L, 1, 20, user))
                .willReturn(PageResponse.of(1, 20, 1, List.of(comment)));

        mockMvc.perform(get("/api/posts/9/comments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].likedByCurrentUser").value(true))
                .andExpect(jsonPath("$.data.records[0].authorNickname").value("夜莺"));
    }

    @Test
    void createsComment() throws Exception {
        SysUser user = normalUser();
        CommentCreateRequest request = new CommentCreateRequest(
                null,
                "补充一个细节：如果出生点靠近伐木场，先观察枪声再决定路线。",
                null,
                null
        );

        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(forumCommentService.createComment(eq(9L), any(CommentCreateRequest.class), eq(user)))
                .willReturn(new CommentCreatedResponse(12L));

        mockMvc.perform(post("/api/posts/9/comments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(12L));
    }

    @Test
    void rejectsInvalidCommentRequest() throws Exception {
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(normalUser()));
        CommentCreateRequest request = new CommentCreateRequest(null, "短", null, null);

        mockMvc.perform(post("/api/posts/9/comments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void rejectsCreateCommentWithoutLogin() throws Exception {
        CommentCreateRequest request = new CommentCreateRequest(
                null,
                "补充一个细节：如果出生点靠近伐木场，先观察枪声再决定路线。",
                null,
                null
        );

        mockMvc.perform(post("/api/posts/9/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void withdrawsOwnComment() throws Exception {
        SysUser user = normalUser();
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(forumCommentService.withdrawComment(9L, 21L, user))
                .willReturn(new CommentWithdrawResponse(21L, 9L, "HIDDEN", 2));

        mockMvc.perform(delete("/api/posts/9/comments/21")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(21L))
                .andExpect(jsonPath("$.data.status").value("HIDDEN"))
                .andExpect(jsonPath("$.data.postCommentCount").value(2));

        verify(forumCommentService).withdrawComment(9L, 21L, user);
    }

    @Test
    void updatesOwnComment() throws Exception {
        SysUser user = normalUser();
        CommentUpdateRequest request = new CommentUpdateRequest("Updated route timing after another raid.");
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(forumCommentService.updateComment(eq(9L), eq(21L), any(CommentUpdateRequest.class), eq(user)))
                .willReturn(new CommentUpdatedResponse(
                        21L,
                        9L,
                        "Updated route timing after another raid.",
                        LocalDateTime.of(2026, 6, 11, 14, 20)
                ));

        mockMvc.perform(put("/api/posts/9/comments/21")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(21L))
                .andExpect(jsonPath("$.data.content").value("Updated route timing after another raid."));
    }

    @Test
    void rejectsInvalidCommentUpdateRequest() throws Exception {
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(normalUser()));
        CommentUpdateRequest request = new CommentUpdateRequest("短");

        mockMvc.perform(put("/api/posts/9/comments/21")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void rejectsUpdateCommentWithoutLogin() throws Exception {
        CommentUpdateRequest request = new CommentUpdateRequest("Updated route timing after another raid.");

        mockMvc.perform(put("/api/posts/9/comments/21")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void rejectsWithdrawCommentWithoutLogin() throws Exception {
        mockMvc.perform(delete("/api/posts/9/comments/21"))
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
