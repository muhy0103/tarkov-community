package com.tarkovcommunity.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String content;
    private String postType;
    private String coverImage;
    private Long mapId;
    private Long traderId;
    private Long questId;
    private Long itemId;
    private Long weaponId;
    private Long ammoId;
    private String riskLevel;
    private String intelStatus;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private String status;
    private Integer pinned;
    private Integer recommended;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
