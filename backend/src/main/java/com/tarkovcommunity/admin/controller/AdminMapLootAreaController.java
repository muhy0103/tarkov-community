package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminMapLootAreaResponse;
import com.tarkovcommunity.admin.dto.AdminMapLootAreaUpdateRequest;
import com.tarkovcommunity.admin.service.AdminMapLootAreaService;
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
@RequestMapping("/api/admin/map-loot-areas")
public class AdminMapLootAreaController {

    private final AdminMapLootAreaService adminMapLootAreaService;

    @GetMapping
    public ApiResponse<PageResponse<AdminMapLootAreaResponse>> listLootAreas(
            @RequestParam(required = false) Long mapId,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminMapLootAreaService.listLootAreas(mapId, riskLevel, keyword, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminMapLootAreaResponse> updateLootArea(
            @PathVariable Long id,
            @Valid @RequestBody AdminMapLootAreaUpdateRequest request
    ) {
        return ApiResponse.success(adminMapLootAreaService.updateLootArea(id, request));
    }
}
