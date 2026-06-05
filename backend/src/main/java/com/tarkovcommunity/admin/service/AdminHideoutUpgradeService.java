package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminHideoutUpgradeResponse;
import com.tarkovcommunity.admin.dto.AdminHideoutUpgradeUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminHideoutUpgradeService {
    PageResponse<AdminHideoutUpgradeResponse> listUpgrades(
            Long stationId,
            String keyword,
            int page,
            int size
    );

    AdminHideoutUpgradeResponse updateUpgrade(Long id, AdminHideoutUpgradeUpdateRequest request);
}
