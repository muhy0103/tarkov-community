package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminItemResponse;
import com.tarkovcommunity.admin.dto.AdminItemUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminItemService {
    PageResponse<AdminItemResponse> listItems(
            String itemType,
            String status,
            Boolean questNeeded,
            Boolean hideoutNeeded,
            Boolean keepSuggestion,
            String keyword,
            int page,
            int size
    );

    AdminItemResponse updateItem(Long id, AdminItemUpdateRequest request);
}
