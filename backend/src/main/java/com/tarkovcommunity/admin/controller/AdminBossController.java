package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminBossResponse;
import com.tarkovcommunity.admin.dto.AdminBossUpdateRequest;
import com.tarkovcommunity.admin.service.AdminBossService;
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
@RequestMapping("/api/admin/bosses")
public class AdminBossController {

    private final AdminBossService adminBossService;

    @GetMapping
    public ApiResponse<PageResponse<AdminBossResponse>> listBosses(
            @RequestParam(required = false) Long mapId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminBossService.listBosses(mapId, status, keyword, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminBossResponse> updateBoss(
            @PathVariable Long id,
            @Valid @RequestBody AdminBossUpdateRequest request
    ) {
        return ApiResponse.success(adminBossService.updateBoss(id, request));
    }
}
