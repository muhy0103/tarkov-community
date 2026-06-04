package com.tarkovcommunity.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminQuestUpdateRequest(
        @NotNull(message = "所属商人不能为空")
        Long traderId,

        @NotBlank(message = "任务英文名不能为空")
        @Size(max = 120, message = "任务英文名不能超过120个字符")
        String nameEn,

        @Size(max = 120, message = "任务中文名不能超过120个字符")
        String nameZh,

        @Size(max = 50, message = "任务类型不能超过50个字符")
        String questType,

        Long mapId,

        @Size(max = 2000, message = "任务说明不能超过2000个字符")
        String description,

        @Size(max = 500, message = "任务奖励不能超过500个字符")
        String rewards,

        @Size(max = 500, message = "后续解锁不能超过500个字符")
        String unlocks,

        @NotBlank(message = "任务状态不能为空")
        @Pattern(regexp = "ENABLED|DISABLED", message = "任务状态不正确")
        String status
) {
}
