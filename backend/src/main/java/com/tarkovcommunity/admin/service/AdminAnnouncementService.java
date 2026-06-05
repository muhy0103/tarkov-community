package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminAnnouncementResponse;
import com.tarkovcommunity.admin.dto.AdminAnnouncementUpsertRequest;
import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.user.entity.SysUser;

public interface AdminAnnouncementService {
    PageResponse<AdminAnnouncementResponse> listAnnouncements(String status, String keyword, int page, int size);

    AdminAnnouncementResponse createAnnouncement(AdminAnnouncementUpsertRequest request, SysUser creator);

    AdminAnnouncementResponse updateAnnouncement(Long id, AdminAnnouncementUpsertRequest request);
}
