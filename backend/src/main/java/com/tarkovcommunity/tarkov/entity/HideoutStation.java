package com.tarkovcommunity.tarkov.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("hideout_station")
public class HideoutStation {
    private Long id;
    private String nameEn;
    private String nameZh;
    private String description;
    private String status;
}
