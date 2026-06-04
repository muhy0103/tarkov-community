package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminItemController;
import com.tarkovcommunity.admin.dto.AdminItemResponse;
import com.tarkovcommunity.admin.dto.AdminItemUpdateRequest;
import com.tarkovcommunity.admin.service.AdminItemService;
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

@WebMvcTest(AdminItemController.class)
class AdminItemControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminItemService adminItemService;

    @Test
    void listsItems() throws Exception {
        given(adminItemService.listItems("Key", "ENABLED", true, false, true, "Dorm", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(itemResponse())));

        mockMvc.perform(get("/api/admin/items")
                        .param("itemType", "Key")
                        .param("status", "ENABLED")
                        .param("questNeeded", "true")
                        .param("hideoutNeeded", "false")
                        .param("keepSuggestion", "true")
                        .param("keyword", "Dorm")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].nameEn").value("Dorm room 206 key"))
                .andExpect(jsonPath("$.data.records[0].itemType").value("Key"))
                .andExpect(jsonPath("$.data.records[0].questNeeded").value(true))
                .andExpect(jsonPath("$.data.records[0].keepSuggestion").value(true));
    }

    @Test
    void updatesItem() throws Exception {
        AdminItemUpdateRequest request = new AdminItemUpdateRequest(
                "Dorm room 206 key",
                "206宿舍钥匙",
                "Key",
                "Rare",
                "1x1",
                true,
                false,
                true,
                "Early quest key worth keeping.",
                "ENABLED"
        );
        given(adminItemService.updateItem(eq(1L), any(AdminItemUpdateRequest.class)))
                .willReturn(itemResponse());

        mockMvc.perform(put("/api/admin/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.rarity").value("Rare"))
                .andExpect(jsonPath("$.data.gridSize").value("1x1"))
                .andExpect(jsonPath("$.data.description").value("Early quest key worth keeping."));
    }

    @Test
    void rejectsInvalidItemUpdate() throws Exception {
        AdminItemUpdateRequest request = new AdminItemUpdateRequest(
                "",
                "206宿舍钥匙",
                "Key",
                "Rare",
                "1x1",
                null,
                false,
                true,
                "说明",
                "UNKNOWN"
        );

        mockMvc.perform(put("/api/admin/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminItemResponse itemResponse() {
        return new AdminItemResponse(
                1L,
                "Dorm room 206 key",
                "206宿舍钥匙",
                "Key",
                "Rare",
                "1x1",
                true,
                false,
                true,
                "Early quest key worth keeping.",
                "ENABLED"
        );
    }
}
