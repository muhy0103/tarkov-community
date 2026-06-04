package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminWeaponResponse;
import com.tarkovcommunity.admin.dto.AdminWeaponUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminWeaponService {
    PageResponse<AdminWeaponResponse> listWeapons(
            String weaponType,
            String caliber,
            String status,
            String keyword,
            int page,
            int size
    );

    AdminWeaponResponse updateWeapon(Long id, AdminWeaponUpdateRequest request);
}
