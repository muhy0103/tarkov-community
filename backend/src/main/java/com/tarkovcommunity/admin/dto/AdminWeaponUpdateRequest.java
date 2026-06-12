package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminWeaponUpdateRequest(
        @NotBlank(message = "武器英文名不能为空")
        @Size(max = 120, message = "武器英文名不能超过120个字符")
        String nameEn,

        @Size(max = 120, message = "武器中文名不能超过120个字符")
        String nameZh,

        @Size(max = 60, message = "武器类型不能超过60个字符")
        String weaponType,

        @Size(max = 60, message = "口径不能超过60个字符")
        String caliber,

        @Size(max = 500, message = "武器说明不能超过500个字符")
        String description,

        @Size(max = 500, message = "Image URL cannot exceed 500 characters")
        String imageUrl,

        @NotBlank(message = "武器状态不能为空")
        @Pattern(regexp = "ENABLED|DISABLED", message = "武器状态不正确")
        String status
) {
}
