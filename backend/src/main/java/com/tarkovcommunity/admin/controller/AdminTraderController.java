package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminTraderResponse;
import com.tarkovcommunity.admin.dto.AdminTraderUpdateRequest;
import com.tarkovcommunity.admin.service.AdminTraderService;
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
@RequestMapping("/api/admin/traders")
public class AdminTraderController {

    private final AdminTraderService adminTraderService;

    @GetMapping
    public ApiResponse<PageResponse<AdminTraderResponse>> listTraders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminTraderService.listTraders(status, keyword, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminTraderResponse> updateTrader(
            @PathVariable Long id,
            @Valid @RequestBody AdminTraderUpdateRequest request
    ) {
        return ApiResponse.success(adminTraderService.updateTrader(id, request));
    }
}
