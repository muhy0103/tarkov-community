package com.tarkovcommunity.forum.controller;

import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.forum.dto.PostActionResponse;
import com.tarkovcommunity.forum.service.ForumReactionService;
import com.tarkovcommunity.user.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}")
public class ForumReactionController {

    private final ForumReactionService forumReactionService;
    private final AuthTokenService authTokenService;

    @PostMapping("/likes/toggle")
    public ApiResponse<PostActionResponse> toggleLike(
            @PathVariable Long postId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        SysUser user = requireUser(authorization);
        return ApiResponse.success(forumReactionService.toggleLike(postId, user.getId()));
    }

    @PostMapping("/favorites/toggle")
    public ApiResponse<PostActionResponse> toggleFavorite(
            @PathVariable Long postId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        SysUser user = requireUser(authorization);
        return ApiResponse.success(forumReactionService.toggleFavorite(postId, user.getId()));
    }

    private SysUser requireUser(String authorization) {
        return authTokenService.resolveUser(authorization)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first"));
    }
}
