package com.tarkovcommunity.admin;

import com.tarkovcommunity.admin.controller.AdminDashboardController;
import com.tarkovcommunity.admin.dto.AdminDashboardSummaryResponse;
import com.tarkovcommunity.admin.service.AdminDashboardService;
import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.user.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminDashboardController.class)
class AdminDashboardControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminDashboardService adminDashboardService;

    @MockitoBean
    private AuthTokenService authTokenService;

    @Test
    void getsDashboardSummary() throws Exception {
        given(authTokenService.resolveUser(eq("Bearer admin-token"))).willReturn(Optional.of(adminUser()));
        given(adminDashboardService.getSummary())
                .willReturn(new AdminDashboardSummaryResponse(
                        3L,
                        12L,
                        24L,
                        8L,
                        5L,
                        5L,
                        4L,
                        4L,
                        3L,
                        3L,
                        4L,
                        3L,
                        2L,
                        6L
                ));

        mockMvc.perform(get("/api/admin/dashboard/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userCount").value(3))
                .andExpect(jsonPath("$.data.postCount").value(12))
                .andExpect(jsonPath("$.data.mapCount").value(5))
                .andExpect(jsonPath("$.data.bossCount").value(3))
                .andExpect(jsonPath("$.data.pendingReportCount").value(2))
                .andExpect(jsonPath("$.data.publishedAnnouncementCount").value(6));
    }

    private static SysUser adminUser() {
        SysUser user = new SysUser();
        user.setRole("ADMIN");
        user.setStatus("NORMAL");
        return user;
    }
}
