package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminMapExtractController;
import com.tarkovcommunity.admin.dto.AdminMapExtractResponse;
import com.tarkovcommunity.admin.dto.AdminMapExtractUpdateRequest;
import com.tarkovcommunity.admin.service.AdminMapExtractService;
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

@WebMvcTest(AdminMapExtractController.class)
class AdminMapExtractControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminMapExtractService adminMapExtractService;

    @Test
    void listsMapExtracts() throws Exception {
        given(adminMapExtractService.listExtracts(1L, "ENABLED", "RUAF", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(extractResponse())));

        mockMvc.perform(get("/api/admin/map-extracts")
                        .param("mapId", "1")
                        .param("status", "ENABLED")
                        .param("keyword", "RUAF")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].mapName").value("海关 / Customs"))
                .andExpect(jsonPath("$.data.records[0].name").value("RUAF Roadblock"));
    }

    @Test
    void updatesMapExtract() throws Exception {
        AdminMapExtractUpdateRequest request = new AdminMapExtractUpdateRequest(
                1L,
                "RUAF Roadblock",
                "All",
                "Green flare active",
                "A vehicle-side checkpoint extract on Customs.",
                "ENABLED"
        );
        given(adminMapExtractService.updateExtract(eq(1L), any(AdminMapExtractUpdateRequest.class)))
                .willReturn(extractResponse());

        mockMvc.perform(put("/api/admin/map-extracts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.conditionText").value("Green flare active"))
                .andExpect(jsonPath("$.data.status").value("ENABLED"));
    }

    @Test
    void rejectsInvalidMapExtractUpdate() throws Exception {
        AdminMapExtractUpdateRequest request = new AdminMapExtractUpdateRequest(
                null,
                "",
                "All",
                "Condition",
                "Description",
                "UNKNOWN"
        );

        mockMvc.perform(put("/api/admin/map-extracts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminMapExtractResponse extractResponse() {
        return new AdminMapExtractResponse(
                1L,
                1L,
                "海关 / Customs",
                "RUAF Roadblock",
                "All",
                "Green flare active",
                "A vehicle-side checkpoint extract on Customs.",
                "ENABLED"
        );
    }
}
