package com.tarkovcommunity.meta;

import com.tarkovcommunity.meta.controller.CommunityMetaController;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.meta.dto.AnnouncementResponse;
import com.tarkovcommunity.meta.dto.CategoryResponse;
import com.tarkovcommunity.meta.dto.TagResponse;
import com.tarkovcommunity.meta.service.CommunityMetaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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

    @Test
    void listsPublishedAnnouncements() throws Exception {
        given(communityMetaService.listPublishedAnnouncements(1, 5))
                .willReturn(PageResponse.of(1, 5, 1, List.of(new AnnouncementResponse(
                        1L,
                        "社区维护提醒",
                        "今晚 23:00 会短暂维护。",
                        LocalDateTime.of(2026, 6, 5, 12, 0),
                        LocalDateTime.of(2026, 6, 5, 12, 10)
                ))));

        mockMvc.perform(get("/api/announcements")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].title").value("社区维护提醒"))
                .andExpect(jsonPath("$.data.records[0].content").value("今晚 23:00 会短暂维护。"));
    }

    @Test
    void getsPublishedAnnouncementDetail() throws Exception {
        given(communityMetaService.getPublishedAnnouncement(3L))
                .willReturn(new AnnouncementResponse(
                        3L,
                        "Patch Notes",
                        "Only published announcements should be readable by visitors.",
                        LocalDateTime.of(2026, 6, 12, 9, 54),
                        LocalDateTime.of(2026, 6, 12, 9, 54)
                ));

        mockMvc.perform(get("/api/announcements/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.title").value("Patch Notes"))
                .andExpect(jsonPath("$.data.content").value("Only published announcements should be readable by visitors."));
    }
}
