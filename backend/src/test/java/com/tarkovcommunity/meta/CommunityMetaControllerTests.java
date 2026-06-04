package com.tarkovcommunity.meta;

import com.tarkovcommunity.meta.controller.CommunityMetaController;
import com.tarkovcommunity.meta.dto.CategoryResponse;
import com.tarkovcommunity.meta.dto.TagResponse;
import com.tarkovcommunity.meta.service.CommunityMetaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommunityMetaController.class)
class CommunityMetaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommunityMetaService communityMetaService;

    @Test
    void listsEnabledCategories() throws Exception {
        given(communityMetaService.listCategories())
                .willReturn(List.of(new CategoryResponse(1L, "战区地图", "maps", "地图路线与撤离点", "Map")));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].code").value("maps"));
    }

    @Test
    void listsEnabledTags() throws Exception {
        given(communityMetaService.listTags())
                .willReturn(List.of(new TagResponse(1L, "新手向", "PLAY_STYLE", "#22C55E")));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("新手向"));
    }
}
