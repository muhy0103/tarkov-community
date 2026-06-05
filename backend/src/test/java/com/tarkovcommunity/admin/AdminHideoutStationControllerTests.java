package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminHideoutStationController;
import com.tarkovcommunity.admin.dto.AdminHideoutStationResponse;
import com.tarkovcommunity.admin.dto.AdminHideoutStationUpdateRequest;
import com.tarkovcommunity.admin.service.AdminHideoutStationService;
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

@WebMvcTest(AdminHideoutStationController.class)
class AdminHideoutStationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminHideoutStationService adminHideoutStationService;

    @Test
    void listsHideoutStations() throws Exception {
        given(adminHideoutStationService.listStations("ENABLED", "med", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(stationResponse())));

        mockMvc.perform(get("/api/admin/hideout/stations")
                        .param("status", "ENABLED")
                        .param("keyword", "med")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].nameEn").value("Medstation"))
                .andExpect(jsonPath("$.data.records[0].nameZh").value("医疗站"));
    }

    @Test
    void updatesHideoutStation() throws Exception {
        AdminHideoutStationUpdateRequest request = new AdminHideoutStationUpdateRequest(
                "Medstation",
                "医疗站",
                "Crafts medical supplies and injector cases.",
                "ENABLED"
        );
        given(adminHideoutStationService.updateStation(eq(1L), any(AdminHideoutStationUpdateRequest.class)))
                .willReturn(stationResponse());

        mockMvc.perform(put("/api/admin/hideout/stations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.description").value("Crafts medical supplies and injector cases."))
                .andExpect(jsonPath("$.data.status").value("ENABLED"));
    }

    @Test
    void rejectsInvalidHideoutStationUpdate() throws Exception {
        AdminHideoutStationUpdateRequest request = new AdminHideoutStationUpdateRequest(
                "",
                "医疗站",
                "说明",
                "UNKNOWN"
        );

        mockMvc.perform(put("/api/admin/hideout/stations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminHideoutStationResponse stationResponse() {
        return new AdminHideoutStationResponse(
                1L,
                "Medstation",
                "医疗站",
                "Crafts medical supplies and injector cases.",
                "ENABLED"
        );
    }
}
