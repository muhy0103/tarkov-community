package com.tarkovcommunity.tarkov.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tarkov_weapon")
public class TarkovWeapon {
    private Long id;
    private String nameEn;
    private String nameZh;
    private String weaponType;
    private String caliber;
    private String description;
    private String imageUrl;
    private String status;
}
