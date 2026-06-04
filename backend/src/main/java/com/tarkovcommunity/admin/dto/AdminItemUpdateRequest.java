package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminItemUpdateRequest(
        @NotBlank(message = "物品英文名不能为空")
        @Size(max = 120, message = "物品英文名不能超过120个字符")
        String nameEn,

        @Size(max = 120, message = "物品中文名不能超过120个字符")
        String nameZh,

        @Size(max = 60, message = "物品类型不能超过60个字符")
        String itemType,

        @Size(max = 40, message = "稀有度不能超过40个字符")
        String rarity,

        @Size(max = 20, message = "格子尺寸不能超过20个字符")
        String gridSize,

        @NotNull(message = "任务需求标记不能为空")
        Boolean questNeeded,

        @NotNull(message = "藏身处需求标记不能为空")
        Boolean hideoutNeeded,

        @NotNull(message = "保留建议标记不能为空")
        Boolean keepSuggestion,

        @Size(max = 500, message = "物品说明不能超过500个字符")
        String description,

        @NotBlank(message = "物品状态不能为空")
        @Pattern(regexp = "ENABLED|DISABLED", message = "物品状态不正确")
        String status
) {
}
