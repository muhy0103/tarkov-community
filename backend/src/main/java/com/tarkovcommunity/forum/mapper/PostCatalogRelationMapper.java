package com.tarkovcommunity.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.forum.entity.Post;
import com.tarkovcommunity.forum.entity.PostCatalogRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostCatalogRelationMapper extends BaseMapper<PostCatalogRelation> {

    @Select("""
            SELECT p.*
            FROM post p
            INNER JOIN post_catalog_relation r ON r.post_id = p.id
            WHERE r.catalog_type = #{catalogType}
              AND r.catalog_id = #{catalogId}
              AND p.status = 'NORMAL'
              AND p.deleted = 0
            ORDER BY p.pinned DESC, p.recommended DESC, p.created_at DESC
            """)
    Page<Post> selectRelatedPostsPage(
            Page<Post> page,
            @Param("catalogType") String catalogType,
            @Param("catalogId") Long catalogId
    );
}
