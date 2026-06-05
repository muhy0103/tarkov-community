package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminAmmoResponse;
import com.tarkovcommunity.admin.dto.AdminAmmoUpdateRequest;
import com.tarkovcommunity.admin.service.AdminAmmoService;
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
@RequestMapping("/api/admin/ammo")
public class AdminAmmoController {

    private final AdminAmmoService adminAmmoService;

    @GetMapping
    public ApiResponse<PageResponse<AdminAmmoResponse>> listAmmo(
            @RequestParam(required = false) String caliber,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminAmmoService.listAmmo(caliber, status, keyword, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminAmmoResponse> updateAmmo(
            @PathVariable Long id,
            @Valid @RequestBody AdminAmmoUpdateRequest request
    ) {
        return ApiResponse.success(adminAmmoService.updateAmmo(id, request));
    }
}
