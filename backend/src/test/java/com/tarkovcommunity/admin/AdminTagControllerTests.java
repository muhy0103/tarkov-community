package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminTagController;
import com.tarkovcommunity.admin.dto.AdminTagResponse;
import com.tarkovcommunity.admin.dto.AdminTagUpdateRequest;
import com.tarkovcommunity.admin.service.AdminTagService;
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

@WebMvcTest(AdminTagController.class)
class AdminTagControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminTagService adminTagService;

    @Test
    void listsTags() throws Exception {
        given(adminTagService.listTags("SYSTEM", "ENABLED", "任务", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(tagResponse())));

        mockMvc.perform(get("/api/admin/tags")
                        .param("type", "SYSTEM")
                        .param("status", "ENABLED")
                        .param("keyword", "任务")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("任务攻略"))
                .andExpect(jsonPath("$.data.records[0].color").value("#2563EB"));
    }

    @Test
    void updatesTag() throws Exception {
        AdminTagUpdateRequest request = new AdminTagUpdateRequest(
                "任务攻略",
                "SYSTEM",
                "#2563EB",
                "ENABLED"
        );
        given(adminTagService.updateTag(eq(1L), any(AdminTagUpdateRequest.class))).willReturn(tagResponse());

        mockMvc.perform(put("/api/admin/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.type").value("SYSTEM"))
                .andExpect(jsonPath("$.data.status").value("ENABLED"));
    }

    @Test
    void rejectsInvalidTagUpdate() throws Exception {
        AdminTagUpdateRequest request = new AdminTagUpdateRequest(
                "",
                "",
                "#123456789",
                "UNKNOWN"
        );

        mockMvc.perform(put("/api/admin/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminTagResponse tagResponse() {
        return new AdminTagResponse(
                1L,
                "任务攻略",
                "SYSTEM",
                "#2563EB",
                "ENABLED"
        );
    }
}
