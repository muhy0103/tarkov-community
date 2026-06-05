package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminHideoutStationResponse;
import com.tarkovcommunity.admin.dto.AdminHideoutStationUpdateRequest;
import com.tarkovcommunity.admin.service.AdminHideoutStationService;
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
@RequestMapping("/api/admin/hideout/stations")
public class AdminHideoutStationController {

    private final AdminHideoutStationService adminHideoutStationService;

    @GetMapping
    public ApiResponse<PageResponse<AdminHideoutStationResponse>> listStations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminHideoutStationService.listStations(status, keyword, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminHideoutStationResponse> updateStation(
            @PathVariable Long id,
            @Valid @RequestBody AdminHideoutStationUpdateRequest request
    ) {
        return ApiResponse.success(adminHideoutStationService.updateStation(id, request));
    }
}
