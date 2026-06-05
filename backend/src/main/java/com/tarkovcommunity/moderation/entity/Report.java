package com.tarkovcommunity.moderation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {
    private Long id;
    private Long reporterId;
    private String targetType;
    private Long targetId;
    private String reason;
    private String description;
    private String status;
    private Long handlerId;
    private String handleResult;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
}
