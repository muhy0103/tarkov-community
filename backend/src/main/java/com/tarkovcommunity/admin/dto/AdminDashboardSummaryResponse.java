package com.tarkovcommunity.admin.dto;

public record AdminDashboardSummaryResponse(
        Long userCount,
        Long postCount,
        Long commentCount,
        Long categoryCount,
        Long mapCount,
        Long traderCount,
        Long questCount,
        Long itemCount,
        Long weaponCount,
        Long ammoCount,
        Long hideoutStationCount,
        Long bossCount,
        Long pendingReportCount,
        Long publishedAnnouncementCount
) {
}
