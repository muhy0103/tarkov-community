package com.tarkovcommunity.tarkov.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tarkov_item")
public class TarkovItem {
    private Long id;
    private String nameEn;
    private String nameZh;
    private String itemType;
    private String rarity;
    private String gridSize;
    private Boolean questNeeded;
    private Boolean hideoutNeeded;
    private Boolean keepSuggestion;
    private String description;
    private String status;
}
