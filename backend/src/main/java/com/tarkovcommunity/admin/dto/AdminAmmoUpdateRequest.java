package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminAmmoUpdateRequest(
        @NotBlank(message = "弹药英文名不能为空")
        @Size(max = 120, message = "弹药英文名不能超过120个字符")
        String nameEn,

        @Size(max = 120, message = "弹药中文名不能超过120个字符")
        String nameZh,

        @NotBlank(message = "口径不能为空")
        @Size(max = 60, message = "口径不能超过60个字符")
        String caliber,

        @Min(value = 0, message = "伤害不能小于0")
        Integer damage,

        @Min(value = 0, message = "穿透不能小于0")
        Integer penetration,

        @Min(value = 0, message = "护甲伤害不能小于0")
        Integer armorDamage,

        @Size(max = 500, message = "弹药说明不能超过500个字符")
        String description,

        @NotBlank(message = "弹药状态不能为空")
        @Pattern(regexp = "ENABLED|DISABLED", message = "弹药状态不正确")
        String status
) {
}
