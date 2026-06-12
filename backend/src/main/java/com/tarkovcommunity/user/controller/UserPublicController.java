package com.tarkovcommunity.user.controller;

import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.user.dto.PublicUserProfileResponse;
import com.tarkovcommunity.user.service.UserPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserPublicController {

    private final UserPublicService userPublicService;

    @GetMapping("/{id}/profile")
    public ApiResponse<PublicUserProfileResponse> getProfile(@PathVariable Long id) {
        return ApiResponse.success(userPublicService.getProfile(id));
    }

    @GetMapping("/{id}/posts")
    public ApiResponse<PageResponse<PostSummaryResponse>> listPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        return ApiResponse.success(userPublicService.listPosts(id, page, size));
    }
}
