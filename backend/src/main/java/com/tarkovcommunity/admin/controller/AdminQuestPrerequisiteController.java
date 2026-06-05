package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminQuestPrerequisiteResponse;
import com.tarkovcommunity.admin.dto.AdminQuestPrerequisiteUpdateRequest;
import com.tarkovcommunity.admin.service.AdminQuestPrerequisiteService;
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
@RequestMapping("/api/admin/quest-prerequisites")
public class AdminQuestPrerequisiteController {

    private final AdminQuestPrerequisiteService adminQuestPrerequisiteService;

    @GetMapping
    public ApiResponse<PageResponse<AdminQuestPrerequisiteResponse>> listQuestPrerequisites(
            @RequestParam(required = false) Long questId,
            @RequestParam(required = false) Long prerequisiteQuestId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminQuestPrerequisiteService.listQuestPrerequisites(
                questId,
                prerequisiteQuestId,
                keyword,
                page,
                size
        ));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminQuestPrerequisiteResponse> updateQuestPrerequisite(
            @PathVariable Long id,
            @Valid @RequestBody AdminQuestPrerequisiteUpdateRequest request
    ) {
        return ApiResponse.success(adminQuestPrerequisiteService.updateQuestPrerequisite(id, request));
    }
}
