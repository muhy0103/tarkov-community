package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminCategoryResponse;
import com.tarkovcommunity.admin.dto.AdminCategoryUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminCategoryService {

    PageResponse<AdminCategoryResponse> listCategories(String status, String keyword, int page, int size);

    AdminCategoryResponse updateCategory(Long id, AdminCategoryUpdateRequest request);
}
