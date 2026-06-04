package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminUserResponse;
import com.tarkovcommunity.admin.dto.AdminUserUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminUserService {

    PageResponse<AdminUserResponse> listUsers(
            String role,
            String status,
            String keyword,
            int page,
            int size
    );

    AdminUserResponse updateUser(Long id, AdminUserUpdateRequest request);
}
