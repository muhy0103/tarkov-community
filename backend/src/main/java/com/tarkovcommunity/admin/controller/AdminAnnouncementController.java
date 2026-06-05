package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminAnnouncementResponse;
import com.tarkovcommunity.admin.dto.AdminAnnouncementUpsertRequest;
import com.tarkovcommunity.admin.service.AdminAnnouncementService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/announcements")
public class AdminAnnouncementController {

    private final AdminAnnouncementService adminAnnouncementService;
    private final AuthTokenService authTokenService;

    @GetMapping
    public ApiResponse<PageResponse<AdminAnnouncementResponse>> listAnnouncements(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminAnnouncementService.listAnnouncements(status, keyword, page, size));
    }

    @PostMapping
    public ApiResponse<AdminAnnouncementResponse> createAnnouncement(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody AdminAnnouncementUpsertRequest request
    ) {
        return ApiResponse.success(adminAnnouncementService.createAnnouncement(request, requireUser(authorization)));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminAnnouncementResponse> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AdminAnnouncementUpsertRequest request
    ) {
        return ApiResponse.success(adminAnnouncementService.updateAnnouncement(id, request));
    }

    private SysUser requireUser(String authorization) {
        return authTokenService.resolveUser(authorization)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录"));
    }
}
