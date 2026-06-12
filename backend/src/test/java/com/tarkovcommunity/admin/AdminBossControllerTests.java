package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminBossController;
import com.tarkovcommunity.admin.dto.AdminBossResponse;
import com.tarkovcommunity.admin.dto.AdminBossUpdateRequest;
import com.tarkovcommunity.admin.service.AdminBossService;
import com.tarkovcommunity.common.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminBossController.class)
class AdminBossControllerTests {

    private static final String IMAGE_URL = "https://assets.tarkov.dev/reshala.webp";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminBossService adminBossService;

    @Test
    void listsBosses() throws Exception {
        given(adminBossService.listBosses(1L, "ENABLED", "reshala", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(bossResponse())));

        mockMvc.perform(get("/api/admin/bosses")
                        .param("mapId", "1")
                        .param("status", "ENABLED")
                        .param("keyword", "reshala")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].nameEn").value("Reshala"))
                .andExpect(jsonPath("$.data.records[0].imageUrl").value(IMAGE_URL))
                .andExpect(jsonPath("$.data.records[0].nameZh").doesNotExist())
                .andExpect(jsonPath("$.data.records[0].mapName").value("海关 / Customs"));
    }

    @Test
    void updatesBoss() throws Exception {
        AdminBossUpdateRequest request = new AdminBossUpdateRequest(
                "Reshala",
                "雷舍拉",
                1L,
                "Customs boss with multiple guards.",
                "Rifle, armor and guards.",
                IMAGE_URL,
                "ENABLED"
        );
        given(adminBossService.updateBoss(eq(1L), any(AdminBossUpdateRequest.class)))
                .willReturn(bossResponse());

        mockMvc.perform(put("/api/admin/bosses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.description").value("Customs boss with multiple guards."))
                .andExpect(jsonPath("$.data.equipmentSummary").value("Rifle, armor and guards."));
    }

    @Test
    void rejectsInvalidBossUpdate() throws Exception {
        AdminBossUpdateRequest request = new AdminBossUpdateRequest(
                "",
                "雷舍拉",
                1L,
                "说明",
                "装备",
                IMAGE_URL,
                "UNKNOWN"
        );

        mockMvc.perform(put("/api/admin/bosses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminBossResponse bossResponse() {
        return new AdminBossResponse(
                1L,
                "Reshala",
                null,
                1L,
                "海关 / Customs",
                "Customs boss with multiple guards.",
                "Rifle, armor and guards.",
                IMAGE_URL,
                "ENABLED"
        );
    }
}
