package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminTraderController;
import com.tarkovcommunity.admin.dto.AdminTraderResponse;
import com.tarkovcommunity.admin.dto.AdminTraderUpdateRequest;
import com.tarkovcommunity.admin.service.AdminTraderService;
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

@WebMvcTest(AdminTraderController.class)
class AdminTraderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminTraderService adminTraderService;

    @Test
    void listsTraders() throws Exception {
        given(adminTraderService.listTraders("ENABLED", "Prapor", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(traderResponse())));

        mockMvc.perform(get("/api/admin/traders")
                        .param("status", "ENABLED")
                        .param("keyword", "Prapor")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].nameEn").value("Prapor"))
                .andExpect(jsonPath("$.data.records[0].nameZh").doesNotExist());
    }

    @Test
    void updatesTrader() throws Exception {
        AdminTraderUpdateRequest request = new AdminTraderUpdateRequest(
                "Prapor",
                null,
                "早期任务和基础武器来源。",
                "默认解锁",
                "https://example.com/prapor.png",
                "ENABLED"
        );
        given(adminTraderService.updateTrader(eq(1L), any(AdminTraderUpdateRequest.class)))
                .willReturn(new AdminTraderResponse(
                        1L,
                        "Prapor",
                        null,
                        "早期任务和基础武器来源。",
                        "默认解锁",
                        "https://example.com/prapor.png",
                        "ENABLED"
                ));

        mockMvc.perform(put("/api/admin/traders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.unlockCondition").value("默认解锁"))
                .andExpect(jsonPath("$.data.avatar").value("https://example.com/prapor.png"));
    }

    @Test
    void rejectsInvalidTraderUpdate() throws Exception {
        AdminTraderUpdateRequest request = new AdminTraderUpdateRequest(
                "",
                null,
                "说明",
                "默认解锁",
                "https://example.com/avatar.png",
                "UNKNOWN"
        );

        mockMvc.perform(put("/api/admin/traders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminTraderResponse traderResponse() {
        return new AdminTraderResponse(
                1L,
                "Prapor",
                null,
                "早期任务和基础武器来源。",
                "默认解锁",
                "https://example.com/prapor.png",
                "ENABLED"
        );
    }
}
