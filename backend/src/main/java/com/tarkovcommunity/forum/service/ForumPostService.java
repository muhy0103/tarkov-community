package com.tarkovcommunity.forum.service;

import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostCreateRequest;
import com.tarkovcommunity.forum.dto.PostCreatedResponse;
import com.tarkovcommunity.forum.dto.PostDetailResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.forum.dto.PostUpdateRequest;
import com.tarkovcommunity.user.entity.SysUser;

public interface ForumPostService {

    PageResponse<PostSummaryResponse> listPosts(
            String categoryCode,
            String keyword,
            String postType,
            Boolean recommended,
            String sort,
            int page,
            int size
    );

    PostDetailResponse getPost(Long id);

    PostCreatedResponse createPost(PostCreateRequest request, SysUser author);

    PostCreatedResponse updatePost(Long id, PostUpdateRequest request, SysUser currentUser);

    PostCreatedResponse withdrawPost(Long id, SysUser currentUser);
}
