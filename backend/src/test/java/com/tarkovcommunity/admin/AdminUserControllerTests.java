package com.tarkovcommunity.admin;

import com.tarkovcommunity.admin.controller.AdminUserController;
import com.tarkovcommunity.admin.dto.AdminUserResponse;
import com.tarkovcommunity.admin.dto.AdminUserUpdateRequest;
import com.tarkovcommunity.admin.service.AdminUserService;
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

@WebMvcTest(AdminUserController.class)
class AdminUserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @Test
    void listsUsersForAdmin() throws Exception {
        given(adminUserService.listUsers(eq("USER"), eq("NORMAL"), eq("pmc"), eq(1), eq(10)))
                .willReturn(PageResponse.of(1, 10, 1, List.of(new AdminUserResponse(
                        2L,
                        "pmc_rookie",
                        "海关萌新",
                        "rookie@example.com",
                        "USER",
                        "NORMAL",
                        12,
                        null,
                        LocalDateTime.of(2026, 6, 4, 20, 30),
                        LocalDateTime.of(2026, 6, 4, 20, 35)
                ))));

        mockMvc.perform(get("/api/admin/users")
                        .param("role", "USER")
                        .param("status", "NORMAL")
                        .param("keyword", "pmc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("pmc_rookie"))
                .andExpect(jsonPath("$.data.records[0].nickname").value("海关萌新"))
                .andExpect(jsonPath("$.data.records[0].role").value("USER"))
                .andExpect(jsonPath("$.data.records[0].status").value("NORMAL"));
    }

    @Test
    void updatesUserRoleAndStatus() throws Exception {
        given(adminUserService.updateUser(eq(2L), any(AdminUserUpdateRequest.class)))
                .willReturn(new AdminUserResponse(
                        2L,
                        "pmc_rookie",
                        "海关萌新",
                        "rookie@example.com",
                        "MODERATOR",
                        "DISABLED",
                        12,
                        null,
                        LocalDateTime.of(2026, 6, 4, 20, 30),
                        LocalDateTime.of(2026, 6, 4, 20, 45)
                ));

        mockMvc.perform(put("/api/admin/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "MODERATOR",
                                  "status": "DISABLED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.role").value("MODERATOR"))
                .andExpect(jsonPath("$.data.status").value("DISABLED"));
    }
}
