package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminCategoryUpdateRequest(
        @NotBlank(message = "分区名称不能为空")
        @Size(max = 50, message = "分区名称不能超过50个字符")
        String name,

        @Size(max = 500, message = "分区说明不能超过500个字符")
        String description,

        @Size(max = 100, message = "分区图标不能超过100个字符")
        String icon,

        @NotNull(message = "排序值不能为空")
        @Min(value = 0, message = "排序值不能小于0")
        Integer sortOrder,

        @NotBlank(message = "分区状态不能为空")
        @Pattern(regexp = "ENABLED|DISABLED", message = "分区状态不正确")
        String status
) {
}
