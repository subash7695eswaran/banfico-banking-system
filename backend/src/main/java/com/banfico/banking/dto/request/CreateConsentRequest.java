package com.banfico.banking.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConsentRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private Long bankAccountId;

    @NotBlank(message = "Consent purpose is required (e.g. Account Information, Payment Initiation)")
    private String purpose;

    @NotBlank(message = "Third Party Provider (TPP) client ID is required")
    private String tppClientId;

    private String scopes;

    @Future(message = "Consent validity date must be in the future")
    private LocalDateTime validUntil;
}
