package com.tarkovcommunity.meta.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("category")
public class Category {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String icon;
    private Integer sortOrder;
    private String status;
}
