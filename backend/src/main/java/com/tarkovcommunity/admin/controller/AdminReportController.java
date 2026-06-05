package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminReportResponse;
import com.tarkovcommunity.admin.dto.AdminReportReviewRequest;
import com.tarkovcommunity.admin.service.AdminReportService;
import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.user.entity.SysUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;
    private final AuthTokenService authTokenService;

    @GetMapping
    public ApiResponse<PageResponse<AdminReportResponse>> listReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminReportService.listReports(status, targetType, keyword, page, size));
    }

    @PutMapping("/{id}/review")
    public ApiResponse<AdminReportResponse> reviewReport(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody AdminReportReviewRequest request
    ) {
        return ApiResponse.success(adminReportService.reviewReport(id, request, requireAdmin(authorization)));
    }

    private SysUser requireAdmin(String authorization) {
        return authTokenService.resolveUser(authorization)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first"));
    }
}
