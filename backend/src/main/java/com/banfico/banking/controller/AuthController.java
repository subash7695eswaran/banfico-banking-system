package com.banfico.banking.controller;

import com.banfico.banking.dto.request.LoginRequest;
import com.banfico.banking.dto.request.RegisterRequest;
import com.banfico.banking.dto.response.ApiResponse;
import com.banfico.banking.dto.response.AuthResponse;
import com.banfico.banking.dto.response.UserProfileResponse;
import com.banfico.banking.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Keycloak Authentication & IAM Gateway", description = "Keycloak OpenID Connect Login, Self-Registration, and Authenticated Identity Profile Resolver")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Keycloak Direct Grant Login", description = "Authenticates user credentials against Keycloak IAM and returns JWT tokens with detected realm roles.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST Login request for user: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Authentication successful", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Keycloak Customer Self-Registration", description = "Registers a new customer in Keycloak with CUSTOMER role and auto-provisions a core banking account.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST Self-registration request for customer: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.created("Customer registration completed successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/me")
    @Operation(summary = "Get Authenticated User Profile", description = "Resolves authenticated identity from Keycloak JWT claims and retrieves mapped customer banking data.")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser() {
        UserProfileResponse profile = authService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.ok("User profile retrieved", profile));
    }
}
