package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminQuestPrerequisiteController;
import com.tarkovcommunity.admin.dto.AdminQuestPrerequisiteResponse;
import com.tarkovcommunity.admin.dto.AdminQuestPrerequisiteUpdateRequest;
import com.tarkovcommunity.admin.service.AdminQuestPrerequisiteService;
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

@WebMvcTest(AdminQuestPrerequisiteController.class)
class AdminQuestPrerequisiteControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminQuestPrerequisiteService adminQuestPrerequisiteService;

    @Test
    void listsQuestPrerequisites() throws Exception {
        given(adminQuestPrerequisiteService.listQuestPrerequisites(2L, 1L, "Debut", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(prerequisiteResponse())));

        mockMvc.perform(get("/api/admin/quest-prerequisites")
                        .param("questId", "2")
                        .param("prerequisiteQuestId", "1")
                        .param("keyword", "Debut")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].questName").value("Checking"))
                .andExpect(jsonPath("$.data.records[0].prerequisiteQuestName").value("Debut"));
    }

    @Test
    void updatesQuestPrerequisite() throws Exception {
        AdminQuestPrerequisiteUpdateRequest request = new AdminQuestPrerequisiteUpdateRequest(2L, 1L);
        given(adminQuestPrerequisiteService.updateQuestPrerequisite(eq(1L), any(AdminQuestPrerequisiteUpdateRequest.class)))
                .willReturn(prerequisiteResponse());

        mockMvc.perform(put("/api/admin/quest-prerequisites/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.questId").value(2))
                .andExpect(jsonPath("$.data.prerequisiteQuestId").value(1));
    }

    @Test
    void rejectsInvalidQuestPrerequisiteUpdate() throws Exception {
        AdminQuestPrerequisiteUpdateRequest request = new AdminQuestPrerequisiteUpdateRequest(null, null);

        mockMvc.perform(put("/api/admin/quest-prerequisites/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminQuestPrerequisiteResponse prerequisiteResponse() {
        return new AdminQuestPrerequisiteResponse(
                1L,
                2L,
                "Checking",
                1L,
                "Debut"
        );
    }
}
