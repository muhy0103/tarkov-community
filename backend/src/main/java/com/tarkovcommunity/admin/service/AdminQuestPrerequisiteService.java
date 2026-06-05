package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminQuestPrerequisiteResponse;
import com.tarkovcommunity.admin.dto.AdminQuestPrerequisiteUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminQuestPrerequisiteService {
    PageResponse<AdminQuestPrerequisiteResponse> listQuestPrerequisites(
            Long questId,
            Long prerequisiteQuestId,
            String keyword,
            int page,
            int size
    );

    AdminQuestPrerequisiteResponse updateQuestPrerequisite(Long id, AdminQuestPrerequisiteUpdateRequest request);
}
