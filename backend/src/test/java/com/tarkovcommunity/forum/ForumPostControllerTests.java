package com.tarkovcommunity.forum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.controller.ForumPostController;
import com.tarkovcommunity.forum.dto.PostCatalogRelationRequest;
import com.tarkovcommunity.forum.dto.PostCreateRequest;
import com.tarkovcommunity.forum.dto.PostCreatedResponse;
import com.tarkovcommunity.forum.dto.PostDetailResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.forum.dto.PostUpdateRequest;
import com.tarkovcommunity.forum.dto.RelatedCatalogResponse;
import com.tarkovcommunity.forum.service.ForumPostService;
import com.tarkovcommunity.user.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @MockitoBean
    private AuthTokenService authTokenService;

    @Test
    void listsPostsWithFilters() throws Exception {
        RelatedCatalogResponse relation = new RelatedCatalogResponse(
                "MAP",
                1L,
                "Customs",
                "Normal",
                null,
                "maps",
                "主要地图"
        );
        PostSummaryResponse summary = new PostSummaryResponse(
                1L,
                "海关新手任务路线整理",
                "从大红仓库到宿舍区的任务路线和撤离点风险说明。",
                "QUEST_GUIDE",
                "任务档案",
                "quests",
                7L,
                "夜莺",
                true,
                23,
                5,
                2,
                LocalDateTime.of(2026, 6, 4, 18, 0),
                List.of(relation)
        );

        given(forumPostService.listPosts("quests", "海关", "QUEST_GUIDE", true, "HOT", null, null, 1, 10))
                .willReturn(PageResponse.of(1, 10, 1, List.of(summary)));

        mockMvc.perform(get("/api/posts")
                        .param("categoryCode", "quests")
                        .param("keyword", "海关")
                        .param("postType", "QUEST_GUIDE")
                        .param("recommended", "true")
                        .param("sort", "HOT")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("海关新手任务路线整理"))
                .andExpect(jsonPath("$.data.records[0].categoryCode").value("quests"))
                .andExpect(jsonPath("$.data.records[0].relations[0].catalogType").value("MAP"))
                .andExpect(jsonPath("$.data.records[0].relations[0].name").value("Customs"));
    }

    @Test
    void listsPostsWithCatalogRelationFilters() throws Exception {
        given(forumPostService.listPosts(null, null, null, null, "LATEST", "MAP", 1L, 1, 10))
                .willReturn(PageResponse.of(1, 10, 0, List.of()));

        mockMvc.perform(get("/api/posts")
                        .param("catalogType", "MAP")
                        .param("catalogId", "1"))
                .andExpect(status().isOk());

        verify(forumPostService).listPosts(null, null, null, null, "LATEST", "MAP", 1L, 1, 10);
    }

    @Test
    void getsPostDetail() throws Exception {
        SysUser user = normalUser();
        RelatedCatalogResponse relation = new RelatedCatalogResponse(
                "MAP",
                1L,
                "Factory",
                "Hard",
                null,
                "maps",
                "配装地图"
        );
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(forumPostService.getPost(eq(1L), eq(user)))
                .willReturn(new PostDetailResponse(
                        1L,
                        7L,
                        "工厂近距离配装讨论",
                        "预算有限时可以优先保证耳机、护甲和高穿弹药。",
                        "LOADOUT",
                        2L,
                        "装备弹药",
                        "loadouts",
                        "疗养院常客",
                        false,
                        31,
                        8,
                        4,
                        LocalDateTime.of(2026, 6, 4, 18, 5),
                        6,
                        true,
                        true,
                        List.of(relation)
                ));

        mockMvc.perform(get("/api/posts/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("工厂近距离配装讨论"))
                .andExpect(jsonPath("$.data.authorId").value(7L))
                .andExpect(jsonPath("$.data.categoryId").value(2L))
                .andExpect(jsonPath("$.data.favoriteCount").value(6))
                .andExpect(jsonPath("$.data.likedByCurrentUser").value(true))
                .andExpect(jsonPath("$.data.favoritedByCurrentUser").value(true))
                .andExpect(jsonPath("$.data.relations[0].catalogType").value("MAP"))
                .andExpect(jsonPath("$.data.relations[0].name").value("Factory"))
                .andExpect(jsonPath("$.data.content").value("预算有限时可以优先保证耳机、护甲和高穿弹药。"));
    }

    @Test
    void returnsUnifiedNotFoundResponse() throws Exception {
        given(forumPostService.getPost(eq(404L), isNull()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "帖子不存在"));

        mockMvc.perform(get("/api/posts/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("帖子不存在"));
    }

    @Test
    void createsPost() throws Exception {
        SysUser user = normalUser();
        PostCreateRequest request = new PostCreateRequest(
                null,
                2L,
                "森林跑图避战路线",
                "这条路线适合前期做任务，重点是避开伐木场高风险区域。",
                "ROUTE_GUIDE",
                null,
                null
        );

        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(forumPostService.createPost(any(PostCreateRequest.class), eq(user)))
                .willReturn(new PostCreatedResponse(9L));

        mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(9L));
    }

    @Test
    void rejectsInvalidCreateRequest() throws Exception {
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(normalUser()));
        PostCreateRequest request = new PostCreateRequest(
                null,
                2L,
                "",
                "内容",
                "ROUTE_GUIDE",
                null,
                null
        );

        mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void rejectsInvalidNestedCatalogRelationOnCreate() throws Exception {
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(normalUser()));
        PostCreateRequest request = new PostCreateRequest(
                null,
                2L,
                "Customs route guide",
                "This route has enough detail for request validation.",
                "ROUTE_GUIDE",
                null,
                List.of(new PostCatalogRelationRequest("BAD_TYPE", 1L, null))
        );

        mockMvc.perform(post("/api/posts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void rejectsCreatePostWithoutLogin() throws Exception {
        PostCreateRequest request = new PostCreateRequest(
                null,
                2L,
                "森林跑图避战路线",
                "这条路线适合前期做任务，重点是避开伐木场高风险区域。",
                "ROUTE_GUIDE",
                null,
                null
        );

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void updatesOwnPost() throws Exception {
        SysUser user = normalUser();
        PostUpdateRequest request = new PostUpdateRequest(
                3L,
                "海岸线任务路线更新版",
                "补充了疗养院外圈和发电站附近的绕行方案，适合低等级玩家参考。",
                "GUIDE",
                null,
                null
        );

        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(forumPostService.updatePost(eq(9L), any(PostUpdateRequest.class), eq(user)))
                .willReturn(new PostCreatedResponse(9L));

        mockMvc.perform(put("/api/posts/9")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(9L));
    }

    @Test
    void withdrawsOwnPost() throws Exception {
        SysUser user = normalUser();
        given(authTokenService.resolveUser(eq("Bearer user-token"))).willReturn(Optional.of(user));
        given(forumPostService.withdrawPost(9L, user)).willReturn(new PostCreatedResponse(9L));

        mockMvc.perform(delete("/api/posts/9")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(9L));
    }

    private static SysUser normalUser() {
        SysUser user = new SysUser();
        user.setId(7L);
        user.setUsername("pmc_rookie");
        user.setRole("USER");
        user.setStatus("NORMAL");
        return user;
    }
}
