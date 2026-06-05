package com.tarkovcommunity.tarkov.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("hideout_upgrade")
public class HideoutUpgrade {
    private Long id;
    private Long stationId;
    private Integer level;
    private String requiredItems;
    private String requiredTime;
    private String unlocks;
}
