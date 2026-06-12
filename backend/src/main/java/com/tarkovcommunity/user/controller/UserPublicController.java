package com.tarkovcommunity.user.controller;

import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.user.dto.FollowActionResponse;
import com.tarkovcommunity.user.dto.PublicUserProfileResponse;
import com.tarkovcommunity.user.entity.SysUser;
import com.tarkovcommunity.user.service.UserPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserPublicController {

    private final UserPublicService userPublicService;
    private final AuthTokenService authTokenService;

    @GetMapping("/{id}/profile")
    public ApiResponse<PublicUserProfileResponse> getProfile(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        return ApiResponse.success(userPublicService.getProfile(id, resolveOptionalUser(authorization)));
    }

    @PostMapping("/{id}/follow")
    public ApiResponse<FollowActionResponse> followUser(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        return ApiResponse.success(userPublicService.followUser(id, requireUser(authorization)));
    }

    @DeleteMapping("/{id}/follow")
    public ApiResponse<FollowActionResponse> unfollowUser(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        return ApiResponse.success(userPublicService.unfollowUser(id, requireUser(authorization)));
    }

    @GetMapping("/{id}/posts")
    public ApiResponse<PageResponse<PostSummaryResponse>> listPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        return ApiResponse.success(userPublicService.listPosts(id, page, size));
    }

    private SysUser requireUser(String authorization) {
        return authTokenService.resolveUser(authorization)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录"));
    }

    private SysUser resolveOptionalUser(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        return authTokenService.resolveUser(authorization).orElse(null);
    }
}
