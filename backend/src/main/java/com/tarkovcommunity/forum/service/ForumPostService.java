package com.tarkovcommunity.forum.service;

import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostCreateRequest;
import com.tarkovcommunity.forum.dto.PostCreatedResponse;
import com.tarkovcommunity.forum.dto.PostDetailResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;

public interface ForumPostService {

    PageResponse<PostSummaryResponse> listPosts(
            String categoryCode,
            String keyword,
            String postType,
            Boolean recommended,
            int page,
            int size
    );

    PostDetailResponse getPost(Long id);

    PostCreatedResponse createPost(PostCreateRequest request);
}
