package com.tarkovcommunity.admin.dto;

public record AdminPostReviewRequest(
        String status,
        Boolean recommended,
        Boolean pinned
) {
}
