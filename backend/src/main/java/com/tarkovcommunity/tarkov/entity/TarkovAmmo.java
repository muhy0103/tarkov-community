package com.tarkovcommunity.tarkov.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tarkov_ammo")
public class TarkovAmmo {
    private Long id;
    private String nameEn;
    private String nameZh;
    private String caliber;
    private Integer damage;
    private Integer penetration;
    private Integer armorDamage;
    private String description;
    private String imageUrl;
    private String status;
}
