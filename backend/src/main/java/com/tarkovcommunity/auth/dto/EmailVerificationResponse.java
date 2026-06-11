package com.tarkovcommunity.auth.dto;

public record EmailVerificationResponse(
        Long userId,
        String username,
        String email,
        String status,
        String message
) {
}
