package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminPostResponse;
import com.tarkovcommunity.admin.dto.AdminPostReviewRequest;
import com.tarkovcommunity.admin.service.AdminPostService;
import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final AdminPostService adminPostService;

    @GetMapping
    public ApiResponse<PageResponse<AdminPostResponse>> listPosts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminPostService.listPosts(status, categoryCode, keyword, page, size));
    }

    @PutMapping("/{id}/review")
    public ApiResponse<AdminPostResponse> reviewPost(
            @PathVariable Long id,
            @Valid @RequestBody AdminPostReviewRequest request
    ) {
        return ApiResponse.success(adminPostService.reviewPost(id, request));
    }
}
