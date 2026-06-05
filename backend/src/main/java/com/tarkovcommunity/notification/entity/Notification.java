package com.tarkovcommunity.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private Long relatedId;
    private Integer readStatus;
    private LocalDateTime createdAt;
}
