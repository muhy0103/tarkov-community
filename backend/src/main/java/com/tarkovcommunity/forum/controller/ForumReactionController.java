package com.tarkovcommunity.forum.controller;

import com.tarkovcommunity.common.ApiResponse;
import com.tarkovcommunity.forum.dto.PostActionRequest;
import com.tarkovcommunity.forum.dto.PostActionResponse;
import com.tarkovcommunity.forum.service.ForumReactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}")
public class ForumReactionController {

    private final ForumReactionService forumReactionService;

    @PostMapping("/likes/toggle")
    public ApiResponse<PostActionResponse> toggleLike(
            @PathVariable Long postId,
            @Valid @RequestBody PostActionRequest request
    ) {
        return ApiResponse.success(forumReactionService.toggleLike(postId, request.userId()));
    }

    @PostMapping("/favorites/toggle")
    public ApiResponse<PostActionResponse> toggleFavorite(
            @PathVariable Long postId,
            @Valid @RequestBody PostActionRequest request
    ) {
        return ApiResponse.success(forumReactionService.toggleFavorite(postId, request.userId()));
    }
}
