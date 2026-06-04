package com.tarkovcommunity.meta.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tag")
public class Tag {
    private Long id;
    private String name;
    private String type;
    private String color;
    private String status;
}
