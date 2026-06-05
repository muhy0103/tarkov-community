package com.tarkovcommunity.moderation.controller;

import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.moderation.dto.ReportCreateRequest;
import com.tarkovcommunity.moderation.dto.ReportCreatedResponse;
import com.tarkovcommunity.moderation.service.ReportService;
import com.tarkovcommunity.user.entity.SysUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final AuthTokenService authTokenService;

    @PostMapping
    public ApiResponse<ReportCreatedResponse> createReport(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        return ApiResponse.success(reportService.createReport(request, requireUser(authorization)));
    }

    private SysUser requireUser(String authorization) {
        return authTokenService.resolveUser(authorization)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first"));
    }
}
