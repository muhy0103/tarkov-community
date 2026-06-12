package com.tarkovcommunity.forum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.forum.dto.PostCatalogRelationRequest;
import com.tarkovcommunity.forum.dto.RelatedCatalogResponse;
import com.tarkovcommunity.forum.entity.Post;

import java.util.List;
import java.util.Map;

public interface PostCatalogRelationService {
    void replaceRelations(Long postId, List<PostCatalogRelationRequest> requests);

    Map<Long, List<RelatedCatalogResponse>> findRelationsByPostIds(List<Long> postIds);

    Page<Post> selectRelatedPostsPage(String catalogType, Long catalogId, int page, int size);
}
