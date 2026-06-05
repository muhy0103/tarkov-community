package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminHideoutUpgradeController;
import com.tarkovcommunity.admin.dto.AdminHideoutUpgradeResponse;
import com.tarkovcommunity.admin.dto.AdminHideoutUpgradeUpdateRequest;
import com.tarkovcommunity.admin.service.AdminHideoutUpgradeService;
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

@WebMvcTest(AdminHideoutUpgradeController.class)
class AdminHideoutUpgradeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminHideoutUpgradeService adminHideoutUpgradeService;

    @Test
    void listsHideoutUpgrades() throws Exception {
        given(adminHideoutUpgradeService.listUpgrades(1L, "toolset", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(upgradeResponse())));

        mockMvc.perform(get("/api/admin/hideout/upgrades")
                        .param("stationId", "1")
                        .param("keyword", "toolset")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].stationName").value("工作台 / Workbench"))
                .andExpect(jsonPath("$.data.records[0].level").value(2));
    }

    @Test
    void updatesHideoutUpgrade() throws Exception {
        AdminHideoutUpgradeUpdateRequest request = new AdminHideoutUpgradeUpdateRequest(
                1L,
                2,
                "Toolset x1, Bolts x5",
                "2h",
                "Unlocks mid-tier weapon crafts"
        );
        given(adminHideoutUpgradeService.updateUpgrade(eq(1L), any(AdminHideoutUpgradeUpdateRequest.class)))
                .willReturn(upgradeResponse());

        mockMvc.perform(put("/api/admin/hideout/upgrades/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.requiredItems").value("Toolset x1, Bolts x5"))
                .andExpect(jsonPath("$.data.unlocks").value("Unlocks mid-tier weapon crafts"));
    }

    @Test
    void rejectsInvalidHideoutUpgradeUpdate() throws Exception {
        AdminHideoutUpgradeUpdateRequest request = new AdminHideoutUpgradeUpdateRequest(
                null,
                0,
                "材料",
                "1h",
                "解锁"
        );

        mockMvc.perform(put("/api/admin/hideout/upgrades/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void rejectsTooLongHideoutUpgradeUnlocks() throws Exception {
        AdminHideoutUpgradeUpdateRequest request = new AdminHideoutUpgradeUpdateRequest(
                1L,
                1,
                "Toolset x1",
                "1h",
                "A".repeat(501)
        );

        mockMvc.perform(put("/api/admin/hideout/upgrades/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminHideoutUpgradeResponse upgradeResponse() {
        return new AdminHideoutUpgradeResponse(
                1L,
                1L,
                "工作台 / Workbench",
                2,
                "Toolset x1, Bolts x5",
                "2h",
                "Unlocks mid-tier weapon crafts"
        );
    }
}
