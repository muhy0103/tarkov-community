package com.tarkovcommunity.forum.controller;

import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostCreateRequest;
import com.tarkovcommunity.forum.dto.PostCreatedResponse;
import com.tarkovcommunity.forum.dto.PostDetailResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.forum.service.ForumPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class ForumPostController {

    private final ForumPostService forumPostService;

    @GetMapping
    public ApiResponse<PageResponse<PostSummaryResponse>> listPosts(
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String postType,
            @RequestParam(required = false) Boolean recommended,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(forumPostService.listPosts(
                categoryCode,
                keyword,
                postType,
                recommended,
                page,
                size
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostDetailResponse> getPost(@PathVariable Long id) {
        return ApiResponse.success(forumPostService.getPost(id));
    }

    @PostMapping
    public ApiResponse<PostCreatedResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
        return ApiResponse.success(forumPostService.createPost(request));
    }
}
