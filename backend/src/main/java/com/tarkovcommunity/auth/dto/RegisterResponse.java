package com.tarkovcommunity.auth.dto;

public record RegisterResponse(
        Long userId,
        String username,
        String email,
        String status,
        String message,
        Boolean mailSent
) {
}
