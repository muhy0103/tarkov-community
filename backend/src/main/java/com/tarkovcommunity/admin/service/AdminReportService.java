package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminReportResponse;
import com.tarkovcommunity.admin.dto.AdminReportReviewRequest;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.user.entity.SysUser;

public interface AdminReportService {

    PageResponse<AdminReportResponse> listReports(
            String status,
            String targetType,
            String keyword,
            int page,
            int size
    );

    AdminReportResponse reviewReport(Long id, AdminReportReviewRequest request, SysUser admin);
}
