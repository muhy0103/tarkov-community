package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminHideoutStationResponse;
import com.tarkovcommunity.admin.dto.AdminHideoutStationUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminHideoutStationService {
    PageResponse<AdminHideoutStationResponse> listStations(
            String status,
            String keyword,
            int page,
            int size
    );

    AdminHideoutStationResponse updateStation(Long id, AdminHideoutStationUpdateRequest request);
}
