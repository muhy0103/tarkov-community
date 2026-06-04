package com.tarkovcommunity.admin.controller;

import com.tarkovcommunity.admin.dto.AdminCommentResponse;
import com.tarkovcommunity.admin.dto.AdminCommentReviewRequest;
import com.tarkovcommunity.admin.service.AdminCommentService;
import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/comments")
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    @GetMapping
    public ApiResponse<PageResponse<AdminCommentResponse>> listComments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(adminCommentService.listComments(status, keyword, page, size));
    }

    @PutMapping("/{id}/review")
    public ApiResponse<AdminCommentResponse> reviewComment(
            @PathVariable Long id,
            @Valid @RequestBody AdminCommentReviewRequest request
    ) {
        return ApiResponse.success(adminCommentService.reviewComment(id, request));
    }
}
