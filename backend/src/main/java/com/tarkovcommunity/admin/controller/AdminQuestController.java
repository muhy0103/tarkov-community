package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminQuestResponse;
import com.tarkovcommunity.admin.dto.AdminQuestUpdateRequest;
import com.tarkovcommunity.admin.service.AdminQuestService;
import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/quests")
public class AdminQuestController {

    private final AdminQuestService adminQuestService;

    @GetMapping
    public ApiResponse<PageResponse<AdminQuestResponse>> listQuests(
            @RequestParam(required = false) Long traderId,
            @RequestParam(required = false) Long mapId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminQuestService.listQuests(traderId, mapId, status, keyword, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminQuestResponse> updateQuest(
            @PathVariable Long id,
            @Valid @RequestBody AdminQuestUpdateRequest request
    ) {
        return ApiResponse.success(adminQuestService.updateQuest(id, request));
    }
}
