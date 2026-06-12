package com.tarkovcommunity.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_follow")
public class UserFollow {
    private Long id;
    private Long userId;
    private Long followedUserId;
    private LocalDateTime createdAt;
}
