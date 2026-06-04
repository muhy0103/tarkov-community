package com.tarkovcommunity.auth.dto;

public record AuthUserResponse(
        Long id,
        String username,
        String nickname,
        String role,
        Integer contribution
) {
}
