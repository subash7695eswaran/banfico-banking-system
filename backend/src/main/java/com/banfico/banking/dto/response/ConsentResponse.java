package com.banfico.banking.dto.response;

import com.banfico.banking.entity.enums.ConsentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentResponse {
    private Long id;
    private String consentReference;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long accountId;
    private String accountNumber;
    private ConsentStatus status;
    private String purpose;
    private String scopes;
    private String tppClientId;
    private LocalDateTime validUntil;
    private LocalDateTime revokedAt;
    private String revocationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
