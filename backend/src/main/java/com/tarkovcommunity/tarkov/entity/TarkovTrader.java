package com.tarkovcommunity.tarkov.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tarkov_trader")
public class TarkovTrader {
    private Long id;
    private String nameEn;
    private String nameZh;
    private String description;
    private String unlockCondition;
    private String avatar;
    private String status;
}
