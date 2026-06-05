package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminBossResponse;
import com.tarkovcommunity.admin.dto.AdminBossUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminBossService {
    PageResponse<AdminBossResponse> listBosses(
            Long mapId,
            String status,
            String keyword,
            int page,
            int size
    );

    AdminBossResponse updateBoss(Long id, AdminBossUpdateRequest request);
}
