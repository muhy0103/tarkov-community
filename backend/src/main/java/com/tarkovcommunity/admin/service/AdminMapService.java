package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminMapResponse;
import com.tarkovcommunity.admin.dto.AdminMapUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminMapService {

    PageResponse<AdminMapResponse> listMaps(String status, String keyword, int page, int size);

    AdminMapResponse updateMap(Long id, AdminMapUpdateRequest request);
}
