package com.tarkovcommunity.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user")
public class SysUser {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
    private String status;
    private Integer contribution;
}
