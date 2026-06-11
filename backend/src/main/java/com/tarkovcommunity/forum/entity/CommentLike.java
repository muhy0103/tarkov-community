package com.tarkovcommunity.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment_like")
public class CommentLike {
    private Long id;
    private Long commentId;
    private Long userId;
    private LocalDateTime createdAt;
}
