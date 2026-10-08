package com.banfico.banking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentAuditLogResponse {
    private Long id;
    private String consentReference;
    private Long customerId;
    private String action;
    private String actorRole;
    private String actorIdentifier;
    private String details;
    private LocalDateTime createdAt;
}
