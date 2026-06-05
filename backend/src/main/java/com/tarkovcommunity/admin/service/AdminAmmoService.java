package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminAmmoResponse;
import com.tarkovcommunity.admin.dto.AdminAmmoUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminAmmoService {
    PageResponse<AdminAmmoResponse> listAmmo(
            String caliber,
            String status,
            String keyword,
            int page,
            int size
    );

    AdminAmmoResponse updateAmmo(Long id, AdminAmmoUpdateRequest request);
}
