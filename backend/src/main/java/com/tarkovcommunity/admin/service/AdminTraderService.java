package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminTraderResponse;
import com.tarkovcommunity.admin.dto.AdminTraderUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminTraderService {

    PageResponse<AdminTraderResponse> listTraders(String status, String keyword, int page, int size);

    AdminTraderResponse updateTrader(Long id, AdminTraderUpdateRequest request);
}
