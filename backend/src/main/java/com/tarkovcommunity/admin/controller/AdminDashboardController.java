package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminDashboardSummaryResponse;
import com.tarkovcommunity.admin.service.AdminDashboardService;
import com.tarkovcommunity.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    public ApiResponse<AdminDashboardSummaryResponse> getSummary() {
        return ApiResponse.success(adminDashboardService.getSummary());
    }
}
