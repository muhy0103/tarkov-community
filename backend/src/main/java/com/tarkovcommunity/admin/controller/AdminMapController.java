package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminMapResponse;
import com.tarkovcommunity.admin.dto.AdminMapUpdateRequest;
import com.tarkovcommunity.admin.service.AdminMapService;
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
@RequestMapping("/api/admin/maps")
public class AdminMapController {

    private final AdminMapService adminMapService;

    @GetMapping
    public ApiResponse<PageResponse<AdminMapResponse>> listMaps(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminMapService.listMaps(status, keyword, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminMapResponse> updateMap(
            @PathVariable Long id,
            @Valid @RequestBody AdminMapUpdateRequest request
    ) {
        return ApiResponse.success(adminMapService.updateMap(id, request));
    }
}
