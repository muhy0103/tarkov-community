package com.tarkovcommunity.forum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post_catalog_relation")
public class PostCatalogRelation {
    private Long id;
    private Long postId;
    private String catalogType;
    private Long catalogId;
    private String relationNote;
    private LocalDateTime createdAt;
}
