package com.tarkovcommunity.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tarkovcommunity.forum.entity.PostComment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PostCommentMapper extends BaseMapper<PostComment> {

    @Select("""
            SELECT COUNT(*)
            FROM post_comment c
            INNER JOIN post p ON p.id = c.post_id
            WHERE c.user_id = #{userId}
              AND c.status = 'NORMAL'
              AND c.deleted = 0
              AND p.status = 'NORMAL'
              AND p.deleted = 0
            """)
    Long selectVisiblePostCommentCount(@Param("userId") Long userId);

    @Select("""
            SELECT c.id, c.post_id, c.user_id, c.parent_id, c.reply_to_user_id,
                   c.content, c.like_count, c.status, c.created_at, c.updated_at, c.deleted
            FROM post_comment c
            INNER JOIN post p ON p.id = c.post_id
            WHERE c.user_id = #{userId}
              AND c.status = 'NORMAL'
              AND c.deleted = 0
              AND p.status = 'NORMAL'
              AND p.deleted = 0
            ORDER BY c.created_at DESC, c.id DESC
            """)
    Page<PostComment> selectVisiblePostCommentsPage(Page<PostComment> page, @Param("userId") Long userId);
}
