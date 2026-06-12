package com.tarkovcommunity.user.service;

import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.forum.dto.PostSummaryResponse;
import com.tarkovcommunity.user.dto.UserCenterCommentResponse;
import com.tarkovcommunity.user.dto.UserCenterSummaryResponse;
import com.tarkovcommunity.user.dto.UserPasswordUpdateRequest;
import com.tarkovcommunity.user.dto.UserProfileUpdateRequest;
import com.tarkovcommunity.user.dto.UserRelationResponse;
import com.tarkovcommunity.user.entity.SysUser;

public interface UserCenterService {

    UserCenterSummaryResponse getSummary(SysUser user);

    PageResponse<PostSummaryResponse> listPosts(SysUser user, int page, int size);

    PageResponse<UserCenterCommentResponse> listComments(SysUser user, int page, int size);

    PageResponse<PostSummaryResponse> listFavorites(SysUser user, int page, int size);

    PageResponse<UserRelationResponse> listFollowing(SysUser user, int page, int size);

    PageResponse<UserRelationResponse> listFollowers(SysUser user, int page, int size);

    UserCenterSummaryResponse updateProfile(SysUser user, UserProfileUpdateRequest request);

    void updatePassword(SysUser user, UserPasswordUpdateRequest request);
}
