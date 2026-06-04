package com.tarkovcommunity.admin.service;

import com.tarkovcommunity.admin.dto.AdminQuestResponse;
import com.tarkovcommunity.admin.dto.AdminQuestUpdateRequest;
import com.tarkovcommunity.common.PageResponse;

public interface AdminQuestService {

    PageResponse<AdminQuestResponse> listQuests(Long traderId, Long mapId, String status, String keyword, int page, int size);

    AdminQuestResponse updateQuest(Long id, AdminQuestUpdateRequest request);
}
