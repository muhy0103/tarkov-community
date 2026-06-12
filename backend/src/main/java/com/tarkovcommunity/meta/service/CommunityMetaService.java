package com.tarkovcommunity.meta.service;

import com.tarkovcommunity.common.PageResponse;
import com.tarkovcommunity.meta.dto.AnnouncementResponse;
import com.tarkovcommunity.meta.dto.CategoryResponse;
import com.tarkovcommunity.meta.dto.TagResponse;

import java.util.List;

public interface CommunityMetaService {
    List<CategoryResponse> listCategories();

    List<TagResponse> listTags();

    PageResponse<AnnouncementResponse> listPublishedAnnouncements(int page, int size);

    AnnouncementResponse getPublishedAnnouncement(Long id);
}
