package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminQuestController;
import com.tarkovcommunity.admin.dto.AdminQuestResponse;
import com.tarkovcommunity.admin.dto.AdminQuestUpdateRequest;
import com.tarkovcommunity.admin.service.AdminQuestService;
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

@WebMvcTest(AdminQuestController.class)
class AdminQuestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminQuestService adminQuestService;

    @Test
    void listsQuests() throws Exception {
        given(adminQuestService.listQuests(1L, 1L, "ENABLED", "Debut", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(questResponse())));

        mockMvc.perform(get("/api/admin/quests")
                        .param("traderId", "1")
                        .param("mapId", "1")
                        .param("status", "ENABLED")
                        .param("keyword", "Debut")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].nameEn").value("Debut"))
                .andExpect(jsonPath("$.data.records[0].traderName").value("普拉波 / Prapor"))
                .andExpect(jsonPath("$.data.records[0].mapName").value("海关 / Customs"));
    }

    @Test
    void updatesQuest() throws Exception {
        AdminQuestUpdateRequest request = new AdminQuestUpdateRequest(
                1L,
                "Debut",
                "首秀",
                "击杀/交付",
                1L,
                "击杀 Scav 并交付霰弹枪。",
                "经验、卢布、声望",
                "后续 Prapor 任务",
                "ENABLED"
        );
        given(adminQuestService.updateQuest(eq(1L), any(AdminQuestUpdateRequest.class)))
                .willReturn(questResponse());

        mockMvc.perform(put("/api/admin/quests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.rewards").value("经验、卢布、声望"))
                .andExpect(jsonPath("$.data.unlocks").value("后续 Prapor 任务"));
    }

    @Test
    void rejectsInvalidQuestUpdate() throws Exception {
        AdminQuestUpdateRequest request = new AdminQuestUpdateRequest(
                null,
                "",
                "首秀",
                "击杀/交付",
                1L,
                "说明",
                "奖励",
                "解锁",
                "UNKNOWN"
        );

        mockMvc.perform(put("/api/admin/quests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminQuestResponse questResponse() {
        return new AdminQuestResponse(
                1L,
                1L,
                "普拉波 / Prapor",
                "Debut",
                "首秀",
                "击杀/交付",
                1L,
                "海关 / Customs",
                "击杀 Scav 并交付霰弹枪。",
                "经验、卢布、声望",
                "后续 Prapor 任务",
                "ENABLED"
        );
    }
}
