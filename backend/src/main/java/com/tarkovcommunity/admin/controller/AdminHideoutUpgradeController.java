package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminHideoutUpgradeResponse;
import com.tarkovcommunity.admin.dto.AdminHideoutUpgradeUpdateRequest;
import com.tarkovcommunity.admin.service.AdminHideoutUpgradeService;
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
@RequestMapping("/api/admin/hideout/upgrades")
public class AdminHideoutUpgradeController {

    private final AdminHideoutUpgradeService adminHideoutUpgradeService;

    @GetMapping
    public ApiResponse<PageResponse<AdminHideoutUpgradeResponse>> listUpgrades(
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminHideoutUpgradeService.listUpgrades(stationId, keyword, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminHideoutUpgradeResponse> updateUpgrade(
            @PathVariable Long id,
            @Valid @RequestBody AdminHideoutUpgradeUpdateRequest request
    ) {
        return ApiResponse.success(adminHideoutUpgradeService.updateUpgrade(id, request));
    }
}
