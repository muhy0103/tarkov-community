package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminMapExtractResponse;
import com.tarkovcommunity.admin.dto.AdminMapExtractUpdateRequest;
import com.tarkovcommunity.admin.service.AdminMapExtractService;
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
@RequestMapping("/api/admin/map-extracts")
public class AdminMapExtractController {

    private final AdminMapExtractService adminMapExtractService;

    @GetMapping
    public ApiResponse<PageResponse<AdminMapExtractResponse>> listExtracts(
            @RequestParam(required = false) Long mapId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminMapExtractService.listExtracts(mapId, status, keyword, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminMapExtractResponse> updateExtract(
            @PathVariable Long id,
            @Valid @RequestBody AdminMapExtractUpdateRequest request
    ) {
        return ApiResponse.success(adminMapExtractService.updateExtract(id, request));
    }
}
