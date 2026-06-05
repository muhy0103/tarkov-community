package com.tarkovcommunity.tarkov.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("map_extract")
public class MapExtract {
    private Long id;
    private Long mapId;
    private String name;
    private String factionLimit;
    private String conditionText;
    private String description;
    private String status;
}
