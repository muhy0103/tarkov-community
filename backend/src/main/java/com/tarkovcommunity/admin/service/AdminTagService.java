package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminTagResponse;
import com.tarkovcommunity.admin.dto.AdminTagUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminTagService {
    PageResponse<AdminTagResponse> listTags(String type, String status, String keyword, int page, int size);

    AdminTagResponse updateTag(Long id, AdminTagUpdateRequest request);
}
