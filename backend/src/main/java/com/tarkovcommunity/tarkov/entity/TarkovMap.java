package com.tarkovcommunity.tarkov.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tarkov_map")
public class TarkovMap {
    private Long id;
    private String nameEn;
    private String nameZh;
    private String difficulty;
    private String description;
    private String recommendedLevel;
    private String imageUrl;
    private String status;
}
