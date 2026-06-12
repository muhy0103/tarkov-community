package com.tarkovcommunity.meta.controller;

import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.meta.dto.AnnouncementResponse;
import com.tarkovcommunity.meta.dto.CategoryResponse;
import com.tarkovcommunity.meta.dto.TagResponse;
import com.tarkovcommunity.meta.service.CommunityMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommunityMetaController {

    private final CommunityMetaService communityMetaService;

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> listCategories() {
        return ApiResponse.success(communityMetaService.listCategories());
    }

    @GetMapping("/tags")
    public ApiResponse<List<TagResponse>> listTags() {
        return ApiResponse.success(communityMetaService.listTags());
    }

    @GetMapping("/announcements")
    public ApiResponse<PageResponse<AnnouncementResponse>> listAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ApiResponse.success(communityMetaService.listPublishedAnnouncements(page, size));
    }

    @GetMapping("/announcements/{id}")
    public ApiResponse<AnnouncementResponse> getAnnouncement(@PathVariable Long id) {
        return ApiResponse.success(communityMetaService.getPublishedAnnouncement(id));
    }
}
