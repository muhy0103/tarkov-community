package com.tarkovcommunity.notification;

import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.notification.controller.NotificationController;
import com.tarkovcommunity.notification.dto.NotificationResponse;
import com.tarkovcommunity.notification.dto.NotificationUnreadCountResponse;
import com.tarkovcommunity.notification.service.NotificationService;
import com.tarkovcommunity.user.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private AuthTokenService authTokenService;

    @Test
    void listsCurrentUserNotifications() throws Exception {
        SysUser user = normalUser();
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(notificationService.listMine(user, 0, 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(notification(11L, 0))));

        mockMvc.perform(get("/api/notifications/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .param("readStatus", "0")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("Report handled"));
    }

    @Test
    void countsCurrentUserUnreadNotifications() throws Exception {
        SysUser user = normalUser();
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(notificationService.countUnread(user)).willReturn(new NotificationUnreadCountResponse(3L));

        mockMvc.perform(get("/api/notifications/me/unread-count")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.unreadCount").value(3));
    }

    @Test
    void marksCurrentUserNotificationAsRead() throws Exception {
        SysUser user = normalUser();
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(notificationService.markRead(user, 11L)).willReturn(notification(11L, 1));

        mockMvc.perform(put("/api/notifications/me/11/read")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.readStatus").value(1));
    }

    @Test
    void rejectsNotificationsWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/notifications/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    private static NotificationResponse notification(Long id, Integer readStatus) {
        return new NotificationResponse(
                id,
                "REPORT_RESULT",
                "Report handled",
                "Your report has been reviewed.",
                31L,
                readStatus,
                LocalDateTime.of(2026, 6, 5, 18, 0)
        );
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
