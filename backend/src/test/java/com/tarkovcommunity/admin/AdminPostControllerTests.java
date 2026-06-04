package com.tarkovcommunity.admin;

import com.tarkovcommunity.admin.controller.AdminPostController;
import com.tarkovcommunity.admin.dto.AdminPostReviewRequest;
import com.tarkovcommunity.admin.dto.AdminPostResponse;
import com.tarkovcommunity.admin.service.AdminPostService;
import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.common.PageResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminPostController.class)
class AdminPostControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminPostService adminPostService;

    @MockitoBean
    private AuthTokenService authTokenService;

    @Test
    void listsPostsForReview() throws Exception {
        given(authTokenService.resolveUser(eq("Bearer admin-token"))).willReturn(Optional.of(adminUser()));
        given(adminPostService.listPosts(eq("NORMAL"), eq("maps"), eq("woods"), eq(1), eq(10)))
                .willReturn(PageResponse.of(1, 10, 1, List.of(new AdminPostResponse(
                        1L,
                        "Woods route review",
                        "GUIDE",
                        "战区地图",
                        "maps",
                        "社区管理员",
                        "NORMAL",
                        true,
                        false,
                        12,
                        6,
                        2,
                        LocalDateTime.of(2026, 6, 4, 20, 10),
                        LocalDateTime.of(2026, 6, 4, 20, 15)
                ))));

        mockMvc.perform(get("/api/admin/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .param("status", "NORMAL")
                        .param("categoryCode", "maps")
                        .param("keyword", "woods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("Woods route review"))
                .andExpect(jsonPath("$.data.records[0].status").value("NORMAL"))
                .andExpect(jsonPath("$.data.records[0].recommended").value(true))
                .andExpect(jsonPath("$.data.records[0].pinned").value(false));
    }

    @Test
    void reviewsPost() throws Exception {
        given(authTokenService.resolveUser(eq("Bearer admin-token"))).willReturn(Optional.of(adminUser()));
        given(adminPostService.reviewPost(eq(1L), any(AdminPostReviewRequest.class)))
                .willReturn(new AdminPostResponse(
                        1L,
                        "Woods route review",
                        "GUIDE",
                        "战区地图",
                        "maps",
                        "社区管理员",
                        "HIDDEN",
                        false,
                        true,
                        12,
                        6,
                        2,
                        LocalDateTime.of(2026, 6, 4, 20, 10),
                        LocalDateTime.of(2026, 6, 4, 20, 20)
                ));

        mockMvc.perform(put("/api/admin/posts/1/review")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "HIDDEN",
                                  "recommended": false,
                                  "pinned": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("HIDDEN"))
                .andExpect(jsonPath("$.data.recommended").value(false))
                .andExpect(jsonPath("$.data.pinned").value(true));
    }

    private static SysUser adminUser() {
        SysUser user = new SysUser();
        user.setRole("ADMIN");
        user.setStatus("NORMAL");
        return user;
    }
}
