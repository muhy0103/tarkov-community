package com.tarkovcommunity.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.moderation.controller.ReportController;
import com.tarkovcommunity.moderation.dto.ReportCreateRequest;
import com.tarkovcommunity.moderation.dto.ReportCreatedResponse;
import com.tarkovcommunity.moderation.service.ReportService;
import com.tarkovcommunity.user.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private AuthTokenService authTokenService;

    @Test
    void createsReportWithCurrentUser() throws Exception {
        SysUser user = normalUser();
        ReportCreateRequest request = new ReportCreateRequest(
                "POST",
                2L,
                "广告刷屏",
                "帖子中存在交易广告。"
        );

        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(reportService.createReport(any(ReportCreateRequest.class), eq(user)))
                .willReturn(new ReportCreatedResponse(
                        5L,
                        "POST",
                        2L,
                        "广告刷屏",
                        "PENDING",
                        LocalDateTime.of(2026, 6, 5, 16, 0)
                ));

        mockMvc.perform(post("/api/reports")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(5L))
                .andExpect(jsonPath("$.data.targetType").value("POST"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void rejectsReportWithoutLogin() throws Exception {
        ReportCreateRequest request = new ReportCreateRequest(
                "COMMENT",
                3L,
                "人身攻击",
                "评论内容包含攻击性表达。"
        );

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    private static SysUser normalUser() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setUsername("pmc_rookie");
        user.setNickname("新手PMC");
        user.setRole("USER");
        user.setStatus("NORMAL");
        return user;
    }
}
