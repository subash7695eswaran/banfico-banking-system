package com.banfico.banking.service;

import com.banfico.banking.dto.request.LoginRequest;
import com.banfico.banking.dto.request.RegisterRequest;
import com.banfico.banking.dto.response.AuthResponse;
import com.banfico.banking.dto.response.UserProfileResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
    UserProfileResponse getCurrentUserProfile();
}
