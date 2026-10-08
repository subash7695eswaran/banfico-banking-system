package com.banfico.banking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Username or Email is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
