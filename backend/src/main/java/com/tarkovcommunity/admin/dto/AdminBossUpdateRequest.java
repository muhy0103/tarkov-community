package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminBossUpdateRequest(
        @NotBlank(message = "Boss 英文名不能为空")
        @Size(max = 120, message = "Boss 英文名不能超过120个字符")
        String nameEn,

        @Size(max = 120, message = "Boss 中文名不能超过120个字符")
        String nameZh,

        Long mapId,

        @Size(max = 500, message = "Boss 说明不能超过500个字符")
        String description,

        @Size(max = 500, message = "Boss 装备摘要不能超过500个字符")
        String equipmentSummary,

        @NotBlank(message = "Boss 状态不能为空")
        @Pattern(regexp = "ENABLED|DISABLED", message = "Boss 状态不正确")
        String status
) {
}
