package com.tarkovcommunity.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.forum.entity.Favorite;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("""
            SELECT COUNT(*)
            FROM favorite f
            INNER JOIN post p ON p.id = f.post_id
            WHERE f.user_id = #{userId}
              AND p.status = 'NORMAL'
              AND p.deleted = 0
            """)
    Long selectVisiblePostFavoriteCount(@Param("userId") Long userId);

    @Select("""
            SELECT f.id, f.post_id, f.user_id, f.created_at
            FROM favorite f
            INNER JOIN post p ON p.id = f.post_id
            WHERE f.user_id = #{userId}
              AND p.status = 'NORMAL'
              AND p.deleted = 0
            ORDER BY f.created_at DESC, f.id DESC
            """)
    Page<Favorite> selectVisiblePostFavoritesPage(Page<Favorite> page, @Param("userId") Long userId);
}
