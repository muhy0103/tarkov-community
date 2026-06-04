package com.tarkovcommunity.auth.dto;

public record AuthResponse(String token, AuthUserResponse user) {
}
