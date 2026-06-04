package com.tarkovcommunity.user.service;

import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.user.dto.UserCenterCommentResponse;
import com.tarkovcommunity.user.dto.UserCenterSummaryResponse;
import com.tarkovcommunity.user.dto.UserProfileUpdateRequest;
import com.tarkovcommunity.user.entity.SysUser;

public interface UserCenterService {

    UserCenterSummaryResponse getSummary(SysUser user);

    PageResponse<PostSummaryResponse> listPosts(SysUser user, int page, int size);

    PageResponse<UserCenterCommentResponse> listComments(SysUser user, int page, int size);

    PageResponse<PostSummaryResponse> listFavorites(SysUser user, int page, int size);

    UserCenterSummaryResponse updateProfile(SysUser user, UserProfileUpdateRequest request);
}
