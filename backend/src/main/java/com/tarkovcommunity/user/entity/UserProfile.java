package com.tarkovcommunity.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_profile")
public class UserProfile {
    private Long id;
    private Long userId;
    private String bio;
    private String favoriteMaps;
    private String playStyle;
    private String serverRegion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
