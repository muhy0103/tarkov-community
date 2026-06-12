package com.tarkovcommunity.user.service;

import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.user.dto.FollowActionResponse;
import com.tarkovcommunity.user.dto.PublicUserProfileResponse;
import com.tarkovcommunity.user.entity.SysUser;

public interface UserPublicService {

    PublicUserProfileResponse getProfile(Long userId, SysUser viewer);

    FollowActionResponse followUser(Long targetUserId, SysUser viewer);

    FollowActionResponse unfollowUser(Long targetUserId, SysUser viewer);

    PageResponse<PostSummaryResponse> listPosts(Long userId, int page, int size);
}
