package com.tarkovcommunity.admin;

import com.tarkovcommunity.admin.controller.AdminDashboardController;
import com.tarkovcommunity.admin.dto.AdminDashboardSummaryResponse;
import com.tarkovcommunity.admin.service.AdminDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    void getsDashboardSummary() throws Exception {
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
                        3L
                ));

        mockMvc.perform(get("/api/admin/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userCount").value(3))
                .andExpect(jsonPath("$.data.postCount").value(12))
                .andExpect(jsonPath("$.data.mapCount").value(5))
                .andExpect(jsonPath("$.data.bossCount").value(3));
    }
}
