package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminWeaponResponse;
import com.tarkovcommunity.admin.dto.AdminWeaponUpdateRequest;
import com.tarkovcommunity.admin.service.AdminWeaponService;
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
@RequestMapping("/api/admin/weapons")
public class AdminWeaponController {

    private final AdminWeaponService adminWeaponService;

    @GetMapping
    public ApiResponse<PageResponse<AdminWeaponResponse>> listWeapons(
            @RequestParam(required = false) String weaponType,
            @RequestParam(required = false) String caliber,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminWeaponService.listWeapons(weaponType, caliber, status, keyword, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminWeaponResponse> updateWeapon(
            @PathVariable Long id,
            @Valid @RequestBody AdminWeaponUpdateRequest request
    ) {
        return ApiResponse.success(adminWeaponService.updateWeapon(id, request));
    }
}
