package com.tarkovcommunity.admin;

import com.tarkovcommunity.admin.controller.AdminCommentController;
import com.tarkovcommunity.admin.dto.AdminCommentResponse;
import com.tarkovcommunity.admin.dto.AdminCommentReviewRequest;
import com.tarkovcommunity.admin.service.AdminCommentService;
import com.tarkovcommunity.common.PageResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCommentController.class)
class AdminCommentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminCommentService adminCommentService;

    @Test
    void listsCommentsForReview() throws Exception {
        given(adminCommentService.listComments(eq("NORMAL"), eq("extract"), eq(1), eq(10)))
                .willReturn(PageResponse.of(1, 10, 1, List.of(new AdminCommentResponse(
                        1L,
                        2L,
                        "Woods route review",
                        "社区管理员",
                        null,
                        "Remember to check the vehicle extract.",
                        "NORMAL",
                        3,
                        LocalDateTime.of(2026, 6, 4, 20, 50),
                        LocalDateTime.of(2026, 6, 4, 20, 55)
                ))));

        mockMvc.perform(get("/api/admin/comments")
                        .param("status", "NORMAL")
                        .param("keyword", "extract"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].postTitle").value("Woods route review"))
                .andExpect(jsonPath("$.data.records[0].authorNickname").value("社区管理员"))
                .andExpect(jsonPath("$.data.records[0].status").value("NORMAL"));
    }

    @Test
    void reviewsComment() throws Exception {
        given(adminCommentService.reviewComment(eq(1L), any(AdminCommentReviewRequest.class)))
                .willReturn(new AdminCommentResponse(
                        1L,
                        2L,
                        "Woods route review",
                        "社区管理员",
                        null,
                        "Remember to check the vehicle extract.",
                        "HIDDEN",
                        3,
                        LocalDateTime.of(2026, 6, 4, 20, 50),
                        LocalDateTime.of(2026, 6, 4, 21, 0)
                ));

        mockMvc.perform(put("/api/admin/comments/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "HIDDEN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("HIDDEN"));
    }
}
