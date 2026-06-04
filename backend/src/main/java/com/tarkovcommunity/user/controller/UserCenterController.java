package com.tarkovcommunity.user.controller;

import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.user.dto.UserCenterCommentResponse;
import com.tarkovcommunity.user.dto.UserCenterSummaryResponse;
import com.tarkovcommunity.user.dto.UserPasswordUpdateRequest;
import com.tarkovcommunity.user.dto.UserProfileUpdateRequest;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.service.UserCenterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
public class UserCenterController {

    private final UserCenterService userCenterService;
    private final AuthTokenService authTokenService;

    @GetMapping("/summary")
    public ApiResponse<UserCenterSummaryResponse> getSummary(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        return ApiResponse.success(userCenterService.getSummary(requireUser(authorization)));
    }

    @GetMapping("/posts")
    public ApiResponse<PageResponse<PostSummaryResponse>> listPosts(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(userCenterService.listPosts(requireUser(authorization), page, size));
    }

    @GetMapping("/comments")
    public ApiResponse<PageResponse<UserCenterCommentResponse>> listComments(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(userCenterService.listComments(requireUser(authorization), page, size));
    }

    @GetMapping("/favorites")
    public ApiResponse<PageResponse<PostSummaryResponse>> listFavorites(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(userCenterService.listFavorites(requireUser(authorization), page, size));
    }

    @PutMapping("/profile")
    public ApiResponse<UserCenterSummaryResponse> updateProfile(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        return ApiResponse.success(userCenterService.updateProfile(requireUser(authorization), request));
    }

    @PutMapping("/password")
    public ApiResponse<Boolean> updatePassword(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody UserPasswordUpdateRequest request
    ) {
        userCenterService.updatePassword(requireUser(authorization), request);
        return ApiResponse.success(true);
    }

    private SysUser requireUser(String authorization) {
        return authTokenService.resolveUser(authorization)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first"));
    }
}
