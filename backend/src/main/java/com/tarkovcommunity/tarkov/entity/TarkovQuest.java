package com.tarkovcommunity.tarkov.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tarkov_quest")
public class TarkovQuest {
    private Long id;
    private Long traderId;
    private String nameEn;
    private String nameZh;
    private String questType;
    private Long mapId;
    private String description;
    private String rewards;
    private String unlocks;
    private String status;
}
