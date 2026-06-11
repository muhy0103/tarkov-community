package com.tarkovcommunity.auth.service;

import com.tarkovcommunity.auth.dto.AuthResponse;
import com.tarkovcommunity.auth.dto.EmailVerificationResponse;
import com.tarkovcommunity.auth.dto.LoginRequest;
import com.tarkovcommunity.auth.dto.RegisterRequest;
import com.tarkovcommunity.auth.dto.RegisterResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);

    EmailVerificationResponse verifyEmail(String token);
}
