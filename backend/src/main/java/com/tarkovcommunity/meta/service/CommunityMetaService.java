package com.tarkovcommunity.meta.service;

import com.tarkovcommunity.meta.dto.CategoryResponse;
import com.tarkovcommunity.meta.dto.TagResponse;

import java.util.List;

public interface CommunityMetaService {
    List<CategoryResponse> listCategories();

    List<TagResponse> listTags();
}
