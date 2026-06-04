package com.tarkovcommunity.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.admin.controller.AdminCategoryController;
import com.tarkovcommunity.admin.dto.AdminCategoryResponse;
import com.tarkovcommunity.admin.dto.AdminCategoryUpdateRequest;
import com.tarkovcommunity.admin.service.AdminCategoryService;
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

@WebMvcTest(AdminCategoryController.class)
class AdminCategoryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminCategoryService adminCategoryService;

    @Test
    void listsCategories() throws Exception {
        given(adminCategoryService.listCategories("ENABLED", "地图", 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(categoryResponse())));

        mockMvc.perform(get("/api/admin/categories")
                        .param("status", "ENABLED")
                        .param("keyword", "地图")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].code").value("maps"))
                .andExpect(jsonPath("$.data.records[0].name").value("战区地图"));
    }

    @Test
    void updatesCategory() throws Exception {
        AdminCategoryUpdateRequest request = new AdminCategoryUpdateRequest(
                "战区地图",
                "地图路线、撤离点和出生点讨论",
                "Map",
                10,
                "ENABLED"
        );
        given(adminCategoryService.updateCategory(eq(1L), any(AdminCategoryUpdateRequest.class)))
                .willReturn(new AdminCategoryResponse(
                        1L,
                        "战区地图",
                        "maps",
                        "地图路线、撤离点和出生点讨论",
                        "Map",
                        10,
                        "ENABLED"
                ));

        mockMvc.perform(put("/api/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.description").value("地图路线、撤离点和出生点讨论"))
                .andExpect(jsonPath("$.data.sortOrder").value(10));
    }

    @Test
    void rejectsInvalidCategoryUpdate() throws Exception {
        AdminCategoryUpdateRequest request = new AdminCategoryUpdateRequest(
                "",
                "说明",
                "Map",
                -1,
                "UNKNOWN"
        );

        mockMvc.perform(put("/api/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private static AdminCategoryResponse categoryResponse() {
        return new AdminCategoryResponse(
                1L,
                "战区地图",
                "maps",
                "地图路线、撤离点和出生点讨论",
                "Map",
                10,
                "ENABLED"
        );
    }
}
