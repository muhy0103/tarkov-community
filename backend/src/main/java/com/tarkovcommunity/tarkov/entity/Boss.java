package com.tarkovcommunity.tarkov.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("boss")
public class Boss {
    private Long id;
    private String nameEn;
    private String nameZh;
    private Long mapId;
    private String description;
    private String equipmentSummary;
    private String imageUrl;
    private String status;
}
