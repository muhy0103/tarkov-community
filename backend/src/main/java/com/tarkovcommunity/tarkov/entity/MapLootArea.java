package com.tarkovcommunity.tarkov.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("map_loot_area")
public class MapLootArea {
    private Long id;
    private Long mapId;
    private String name;
    private String lootType;
    private String riskLevel;
    private String description;
}
