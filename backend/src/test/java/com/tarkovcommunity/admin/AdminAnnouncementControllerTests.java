package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminAnnouncementController;
import com.tarkovcommunity.admin.dto.AdminAnnouncementResponse;
import com.tarkovcommunity.admin.dto.AdminAnnouncementUpsertRequest;
import com.tarkovcommunity.admin.service.AdminAnnouncementService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAnnouncementController.class)
class AdminAnnouncementControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminAnnouncementService adminAnnouncementService;

    @MockitoBean
    private AuthTokenService authTokenService;

    @Test
    void listsAnnouncements() throws Exception {
        given(adminAnnouncementService.listAnnouncements("PUBLISHED", "维护", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(announcementResponse())));

        mockMvc.perform(get("/api/admin/announcements")
                        .param("status", "PUBLISHED")
                        .param("keyword", "维护")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("服务器维护提醒"))
                .andExpect(jsonPath("$.data.records[0].creatorNickname").value("管理员"));
    }

    @Test
    void createsAnnouncementWithCurrentAdmin() throws Exception {
        AdminAnnouncementUpsertRequest request = new AdminAnnouncementUpsertRequest(
                "服务器维护提醒",
                "今晚 23:00 将进行社区系统维护。",
                "PUBLISHED"
        );
        SysUser admin = admin();
        given(authTokenService.resolveUser("Bearer demo-token")).willReturn(Optional.of(admin));
        given(adminAnnouncementService.createAnnouncement(any(AdminAnnouncementUpsertRequest.class), eq(admin)))
                .willReturn(announcementResponse());

        mockMvc.perform(post("/api/admin/announcements")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer demo-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("服务器维护提醒"))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    void updatesAnnouncement() throws Exception {
        AdminAnnouncementUpsertRequest request = new AdminAnnouncementUpsertRequest(
                "服务器维护提醒",
                "今晚 23:00 将进行社区系统维护。",
                "DRAFT"
        );
        given(adminAnnouncementService.updateAnnouncement(eq(1L), any(AdminAnnouncementUpsertRequest.class)))
                .willReturn(new AdminAnnouncementResponse(
                        1L,
                        "服务器维护提醒",
                        "今晚 23:00 将进行社区系统维护。",
                        "DRAFT",
                        1L,
                        "管理员",
                        LocalDateTime.of(2026, 6, 5, 12, 0),
                        LocalDateTime.of(2026, 6, 5, 12, 30)
                ));

        mockMvc.perform(put("/api/admin/announcements/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void rejectsInvalidAnnouncementCreate() throws Exception {
        AdminAnnouncementUpsertRequest request = new AdminAnnouncementUpsertRequest(
                "",
                "",
                "UNKNOWN"
        );

        mockMvc.perform(post("/api/admin/announcements")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer demo-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminAnnouncementResponse announcementResponse() {
        return new AdminAnnouncementResponse(
                1L,
                "服务器维护提醒",
                "今晚 23:00 将进行社区系统维护。",
                "PUBLISHED",
                1L,
                "管理员",
                LocalDateTime.of(2026, 6, 5, 12, 0),
                LocalDateTime.of(2026, 6, 5, 12, 10)
        );
    }

    private static SysUser admin() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setNickname("管理员");
        user.setRole("ADMIN");
        user.setStatus("NORMAL");
        return user;
    }
}
