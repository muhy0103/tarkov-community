package com.tarkovcommunity.notification.controller;

import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.notification.dto.NotificationReadAllResponse;
import com.tarkovcommunity.notification.dto.NotificationResponse;
import com.tarkovcommunity.notification.dto.NotificationUnreadCountResponse;
import com.tarkovcommunity.notification.service.NotificationService;
import com.tarkovcommunity.user.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications/me")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthTokenService authTokenService;

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> listMine(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(required = false) Integer readStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(notificationService.listMine(requireUser(authorization), readStatus, page, size));
    }

    @GetMapping("/unread-count")
    public ApiResponse<NotificationUnreadCountResponse> countUnread(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        return ApiResponse.success(notificationService.countUnread(requireUser(authorization)));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markRead(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long id
    ) {
        return ApiResponse.success(notificationService.markRead(requireUser(authorization), id));
    }

    @PutMapping("/read-all")
    public ApiResponse<NotificationReadAllResponse> markAllRead(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        return ApiResponse.success(notificationService.markAllRead(requireUser(authorization)));
    }

    private SysUser requireUser(String authorization) {
        return authTokenService.resolveUser(authorization)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first"));
    }
}
