package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminReportController;
import com.tarkovcommunity.admin.dto.AdminReportResponse;
import com.tarkovcommunity.admin.dto.AdminReportReviewRequest;
import com.tarkovcommunity.admin.service.AdminReportService;
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

@WebMvcTest(AdminReportController.class)
class AdminReportControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminReportService adminReportService;

    @MockitoBean
    private AuthTokenService authTokenService;

    @Test
    void listsReportsForReview() throws Exception {
        given(authTokenService.resolveUser(eq("Bearer admin-token"))).willReturn(Optional.of(adminUser()));
        given(adminReportService.listReports("PENDING", "POST", "广告", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(reportResponse("PENDING"))));

        mockMvc.perform(get("/api/admin/reports")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .param("status", "PENDING")
                        .param("targetType", "POST")
                        .param("keyword", "广告"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].reporterNickname").value("新手PMC"))
                .andExpect(jsonPath("$.data.records[0].targetTitle").value("海关任务路线"))
                .andExpect(jsonPath("$.data.records[0].status").value("PENDING"));
    }

    @Test
    void reviewsReportWithCurrentAdmin() throws Exception {
        SysUser admin = adminUser();
        given(authTokenService.resolveUser(eq("Bearer admin-token"))).willReturn(Optional.of(admin));
        given(adminReportService.reviewReport(eq(1L), any(AdminReportReviewRequest.class), eq(admin)))
                .willReturn(reportResponse("RESOLVED"));

        AdminReportReviewRequest request = new AdminReportReviewRequest(
                "RESOLVED",
                "已隐藏违规内容。",
                false
        );

        mockMvc.perform(put("/api/admin/reports/1/review")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.handlerNickname").value("管理员"));
    }

    private static AdminReportResponse reportResponse(String status) {
        return new AdminReportResponse(
                1L,
                7L,
                "新手PMC",
                "POST",
                2L,
                "海关任务路线",
                "这是一篇需要处理的帖子。",
                "广告刷屏",
                "帖子中存在交易广告。",
                status,
                "RESOLVED".equals(status) ? 9L : null,
                "RESOLVED".equals(status) ? "管理员" : "未处理",
                "RESOLVED".equals(status) ? "已隐藏违规内容。" : null,
                "RESOLVED".equals(status) ? LocalDateTime.of(2026, 6, 5, 16, 30) : null,
                LocalDateTime.of(2026, 6, 5, 16, 0)
        );
    }

    private static SysUser adminUser() {
        SysUser user = new SysUser();
        user.setId(9L);
        user.setUsername("admin");
        user.setNickname("管理员");
        user.setRole("ADMIN");
        user.setStatus("NORMAL");
        return user;
    }
}
