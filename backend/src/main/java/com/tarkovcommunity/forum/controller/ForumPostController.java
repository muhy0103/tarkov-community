package com.tarkovcommunity.forum.controller;

import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostCreateRequest;
import com.tarkovcommunity.forum.dto.PostCreatedResponse;
import com.tarkovcommunity.forum.dto.PostDetailResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.forum.dto.PostUpdateRequest;
import com.tarkovcommunity.forum.service.ForumPostService;
import com.tarkovcommunity.user.entity.SysUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/posts")
public class ForumPostController {

    private final ForumPostService forumPostService;
    private final AuthTokenService authTokenService;

    @GetMapping
    public ApiResponse<PageResponse<PostSummaryResponse>> listPosts(
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String postType,
            @RequestParam(required = false) Boolean recommended,
            @RequestParam(defaultValue = "LATEST") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(forumPostService.listPosts(
                categoryCode,
                keyword,
                postType,
                recommended,
                sort,
                page,
                size
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostDetailResponse> getPost(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        return ApiResponse.success(forumPostService.getPost(id, resolveOptionalUser(authorization)));
    }

    @PostMapping
    public ApiResponse<PostCreatedResponse> createPost(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody PostCreateRequest request
    ) {
        return ApiResponse.success(forumPostService.createPost(request, requireUser(authorization)));
    }

    @PutMapping("/{id}")
    public ApiResponse<PostCreatedResponse> updatePost(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        return ApiResponse.success(forumPostService.updatePost(id, request, requireUser(authorization)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<PostCreatedResponse> withdrawPost(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        return ApiResponse.success(forumPostService.withdrawPost(id, requireUser(authorization)));
    }

    private SysUser requireUser(String authorization) {
        return authTokenService.resolveUser(authorization)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login first"));
    }

    private SysUser resolveOptionalUser(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        return authTokenService.resolveUser(authorization).orElse(null);
    }
}
