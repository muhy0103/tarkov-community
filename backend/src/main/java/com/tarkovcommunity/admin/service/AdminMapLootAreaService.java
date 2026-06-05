package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminMapLootAreaResponse;
import com.tarkovcommunity.admin.dto.AdminMapLootAreaUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminMapLootAreaService {
    PageResponse<AdminMapLootAreaResponse> listLootAreas(
            Long mapId,
            String riskLevel,
            String keyword,
            int page,
            int size
    );

    AdminMapLootAreaResponse updateLootArea(Long id, AdminMapLootAreaUpdateRequest request);
}
