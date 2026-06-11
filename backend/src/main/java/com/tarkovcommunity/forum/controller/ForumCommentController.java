package com.tarkovcommunity.forum.controller;

import com.tarkovcommunity.auth.service.AuthTokenService;
import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.CommentCreateRequest;
import com.tarkovcommunity.forum.dto.CommentCreatedResponse;
import com.tarkovcommunity.forum.dto.CommentResponse;
import com.tarkovcommunity.forum.dto.CommentWithdrawResponse;
import com.tarkovcommunity.forum.service.ForumCommentService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/comments")
public class ForumCommentController {

    private final ForumCommentService forumCommentService;
    private final AuthTokenService authTokenService;

    @GetMapping
    public ApiResponse<PageResponse<CommentResponse>> listComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        return ApiResponse.success(forumCommentService.listComments(postId, page, size, resolveOptionalUser(authorization)));
    }

    @PostMapping
    public ApiResponse<CommentCreatedResponse> createComment(
            @PathVariable Long postId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        return ApiResponse.success(forumCommentService.createComment(postId, request, requireUser(authorization)));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<CommentWithdrawResponse> withdrawComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        return ApiResponse.success(forumCommentService.withdrawComment(postId, commentId, requireUser(authorization)));
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
