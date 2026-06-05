package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminMapLootAreaController;
import com.tarkovcommunity.admin.dto.AdminMapLootAreaResponse;
import com.tarkovcommunity.admin.dto.AdminMapLootAreaUpdateRequest;
import com.tarkovcommunity.admin.service.AdminMapLootAreaService;
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

@WebMvcTest(AdminMapLootAreaController.class)
class AdminMapLootAreaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminMapLootAreaService adminMapLootAreaService;

    @Test
    void listsMapLootAreas() throws Exception {
        given(adminMapLootAreaService.listLootAreas(1L, "HIGH", "Dorms", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(lootAreaResponse())));

        mockMvc.perform(get("/api/admin/map-loot-areas")
                        .param("mapId", "1")
                        .param("riskLevel", "HIGH")
                        .param("keyword", "Dorms")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].mapName").value("海关 / Customs"))
                .andExpect(jsonPath("$.data.records[0].name").value("Dorms Marked Room"));
    }

    @Test
    void updatesMapLootArea() throws Exception {
        AdminMapLootAreaUpdateRequest request = new AdminMapLootAreaUpdateRequest(
                1L,
                "Dorms Marked Room",
                "High-value room",
                "HIGH",
                "Marked key room with frequent player traffic."
        );
        given(adminMapLootAreaService.updateLootArea(eq(1L), any(AdminMapLootAreaUpdateRequest.class)))
                .willReturn(lootAreaResponse());

        mockMvc.perform(put("/api/admin/map-loot-areas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.lootType").value("High-value room"))
                .andExpect(jsonPath("$.data.riskLevel").value("HIGH"));
    }

    @Test
    void rejectsInvalidMapLootAreaUpdate() throws Exception {
        AdminMapLootAreaUpdateRequest request = new AdminMapLootAreaUpdateRequest(
                null,
                "",
                "Loot",
                "Risk",
                "Description"
        );

        mockMvc.perform(put("/api/admin/map-loot-areas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminMapLootAreaResponse lootAreaResponse() {
        return new AdminMapLootAreaResponse(
                1L,
                1L,
                "海关 / Customs",
                "Dorms Marked Room",
                "High-value room",
                "HIGH",
                "Marked key room with frequent player traffic."
        );
    }
}
