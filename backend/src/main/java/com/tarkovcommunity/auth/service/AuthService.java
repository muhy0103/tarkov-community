package com.tarkovcommunity.auth.service;

import com.tarkovcommunity.auth.dto.AuthResponse;
import com.tarkovcommunity.auth.dto.LoginRequest;
import com.tarkovcommunity.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);
}
