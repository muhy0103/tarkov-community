package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminTraderUpdateRequest(
        @NotBlank(message = "商人英文名不能为空")
        @Size(max = 80, message = "商人英文名不能超过80个字符")
        String nameEn,

        @NotBlank(message = "商人中文名不能为空")
        @Size(max = 80, message = "商人中文名不能超过80个字符")
        String nameZh,

        @Size(max = 500, message = "商人说明不能超过500个字符")
        String description,

        @Size(max = 255, message = "解锁条件不能超过255个字符")
        String unlockCondition,

        @Size(max = 500, message = "头像地址不能超过500个字符")
        String avatar,

        @NotBlank(message = "商人状态不能为空")
        @Pattern(regexp = "ENABLED|DISABLED", message = "商人状态不正确")
        String status
) {
}
