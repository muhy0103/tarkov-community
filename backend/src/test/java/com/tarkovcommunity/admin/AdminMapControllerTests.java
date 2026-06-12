package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminMapController;
import com.tarkovcommunity.admin.dto.AdminMapResponse;
import com.tarkovcommunity.admin.dto.AdminMapUpdateRequest;
import com.tarkovcommunity.admin.service.AdminMapService;
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

@WebMvcTest(AdminMapController.class)
class AdminMapControllerTests {

    private static final String IMAGE_URL = "https://assets.tarkov.dev/woods.webp";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminMapService adminMapService;

    @Test
    void listsMaps() throws Exception {
        given(adminMapService.listMaps("ENABLED", "woods", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(mapResponse())));

        mockMvc.perform(get("/api/admin/maps")
                        .param("status", "ENABLED")
                        .param("keyword", "woods")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].nameEn").value("Woods"))
                .andExpect(jsonPath("$.data.records[0].imageUrl").value(IMAGE_URL))
                .andExpect(jsonPath("$.data.records[0].nameZh").value("森林"));
    }

    @Test
    void updatesMap() throws Exception {
        AdminMapUpdateRequest request = new AdminMapUpdateRequest(
                "Woods",
                "森林",
                "中等",
                "适合任务推进、远距离交战和隐藏物资搜集。",
                "5+",
                IMAGE_URL,
                "ENABLED"
        );
        given(adminMapService.updateMap(eq(1L), any(AdminMapUpdateRequest.class)))
                .willReturn(new AdminMapResponse(
                        1L,
                        "Woods",
                        "森林",
                        "中等",
                        "适合任务推进、远距离交战和隐藏物资搜集。",
                        "5+",
                        IMAGE_URL,
                        "ENABLED"
                ));

        mockMvc.perform(put("/api/admin/maps/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.description").value("适合任务推进、远距离交战和隐藏物资搜集。"))
                .andExpect(jsonPath("$.data.recommendedLevel").value("5+"));
    }

    @Test
    void rejectsInvalidMapUpdate() throws Exception {
        AdminMapUpdateRequest request = new AdminMapUpdateRequest(
                "",
                "",
                "难度字段过长难度字段过长难度字段过长难度字段过长",
                "说明",
                "5+",
                IMAGE_URL,
                "UNKNOWN"
        );

        mockMvc.perform(put("/api/admin/maps/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminMapResponse mapResponse() {
        return new AdminMapResponse(
                1L,
                "Woods",
                "森林",
                "中等",
                "适合任务推进、远距离交战和隐藏物资搜集。",
                "5+",
                IMAGE_URL,
                "ENABLED"
        );
    }
}
