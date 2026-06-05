package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminHideoutStationUpdateRequest(
        @NotBlank(message = "藏身处设施英文名不能为空")
        @Size(max = 120, message = "藏身处设施英文名不能超过120个字符")
        String nameEn,

        @Size(max = 120, message = "藏身处设施中文名不能超过120个字符")
        String nameZh,

        @Size(max = 500, message = "藏身处设施说明不能超过500个字符")
        String description,

        @NotBlank(message = "藏身处设施状态不能为空")
        @Pattern(regexp = "ENABLED|DISABLED", message = "藏身处设施状态不正确")
        String status
) {
}
