package com.banfico.banking.dto.response;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String idToken;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private String email;
    private String fullName;
    private String primaryRole;
    private Set<String> roles;
    private Long customerId;
}
