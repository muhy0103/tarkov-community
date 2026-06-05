package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminMapExtractResponse;
import com.tarkovcommunity.admin.dto.AdminMapExtractUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminMapExtractService {
    PageResponse<AdminMapExtractResponse> listExtracts(
            Long mapId,
            String status,
            String keyword,
            int page,
            int size
    );

    AdminMapExtractResponse updateExtract(Long id, AdminMapExtractUpdateRequest request);
}
