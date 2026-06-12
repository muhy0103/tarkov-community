package com.tarkovcommunity.forum.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PostCatalogRelationRequest(
        @NotNull(message = "资料类型不能为空")
        @Pattern(regexp = "MAP|TRADER|QUEST|ITEM|WEAPON|AMMO|BOSS|HIDEOUT", message = "资料类型不正确")
        String catalogType,

        @NotNull(message = "资料 ID 不能为空")
        Long catalogId,

        @Size(max = 120, message = "关联备注不能超过 120 个字符")
        String relationNote
) {
}
