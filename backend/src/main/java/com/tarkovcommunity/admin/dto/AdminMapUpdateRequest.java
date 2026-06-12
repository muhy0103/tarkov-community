package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminMapUpdateRequest(
        @NotBlank(message = "地图英文名不能为空")
        @Size(max = 80, message = "地图英文名不能超过80个字符")
        String nameEn,

        @NotBlank(message = "地图中文名不能为空")
        @Size(max = 80, message = "地图中文名不能超过80个字符")
        String nameZh,

        @Size(max = 30, message = "地图难度不能超过30个字符")
        String difficulty,

        @Size(max = 500, message = "地图说明不能超过500个字符")
        String description,

        @Size(max = 50, message = "推荐等级不能超过50个字符")
        String recommendedLevel,

        @Size(max = 500, message = "Image URL cannot exceed 500 characters")
        String imageUrl,

        @NotBlank(message = "地图状态不能为空")
        @Pattern(regexp = "ENABLED|DISABLED", message = "地图状态不正确")
        String status
) {
}
