package com.tarkovcommunity.user.service;

import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.user.dto.PublicUserProfileResponse;

public interface UserPublicService {

    PublicUserProfileResponse getProfile(Long userId);

    PageResponse<PostSummaryResponse> listPosts(Long userId, int page, int size);
}
