package com.tarkovcommunity.forum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.controller.ForumPostController;
import com.tarkovcommunity.forum.dto.PostCreateRequest;
import com.tarkovcommunity.forum.dto.PostCreatedResponse;
import com.tarkovcommunity.forum.dto.PostDetailResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.forum.service.ForumPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(ForumPostController.class)
class ForumPostControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ForumPostService forumPostService;

    @Test
    void listsPostsWithFilters() throws Exception {
        PostSummaryResponse summary = new PostSummaryResponse(
                1L,
                "海关新手任务路线整理",
                "从大红仓库到宿舍区的任务路线和撤离点风险说明。",
                "QUEST_GUIDE",
                "任务档案",
                "quests",
                "夜莺",
                true,
                23,
                5,
                2,
                LocalDateTime.of(2026, 6, 4, 18, 0)
        );

        given(forumPostService.listPosts("quests", "海关", "QUEST_GUIDE", true, 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(summary)));

        mockMvc.perform(get("/api/posts")
                        .param("categoryCode", "quests")
                        .param("keyword", "海关")
                        .param("postType", "QUEST_GUIDE")
                        .param("recommended", "true")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("海关新手任务路线整理"))
                .andExpect(jsonPath("$.data.records[0].categoryCode").value("quests"));
    }

    @Test
    void getsPostDetail() throws Exception {
        given(forumPostService.getPost(1L))
                .willReturn(new PostDetailResponse(
                        1L,
                        "工厂近距离配装讨论",
                        "预算有限时可以优先保证耳机、护甲和高穿弹药。",
                        "LOADOUT",
                        "装备弹药",
                        "loadouts",
                        "疗养院常客",
                        false,
                        31,
                        8,
                        4,
                        LocalDateTime.of(2026, 6, 4, 18, 5)
                ));

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("工厂近距离配装讨论"))
                .andExpect(jsonPath("$.data.content").value("预算有限时可以优先保证耳机、护甲和高穿弹药。"));
    }

    @Test
    void returnsUnifiedNotFoundResponse() throws Exception {
        given(forumPostService.getPost(404L))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "帖子不存在"));

        mockMvc.perform(get("/api/posts/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("帖子不存在"));
    }

    @Test
    void createsPost() throws Exception {
        PostCreateRequest request = new PostCreateRequest(
                1L,
                2L,
                "森林跑图避战路线",
                "这条路线适合前期做任务，重点是避开伐木场高风险区域。",
                "ROUTE_GUIDE",
                null
        );

        given(forumPostService.createPost(any(PostCreateRequest.class)))
                .willReturn(new PostCreatedResponse(9L));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(9L));
    }

    @Test
    void rejectsInvalidCreateRequest() throws Exception {
        PostCreateRequest request = new PostCreateRequest(
                null,
                2L,
                "",
                "内容",
                "ROUTE_GUIDE",
                null
        );

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }
}
