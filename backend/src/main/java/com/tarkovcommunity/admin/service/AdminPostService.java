package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminPostResponse;
import com.tarkovcommunity.common.PageResponse;

public interface AdminPostService {

    PageResponse<AdminPostResponse> listPosts(
            String status,
            String categoryCode,
            String keyword,
            int page,
            int size
    );
}
