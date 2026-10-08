package com.banfico.banking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenBankingDataResponse {
    private String consentReference;
    private String tppClientId;
    private String purpose;
    private String authorizedScopes;
    private Long customerId;
    private String customerName;
    private LocalDateTime validUntil;
    private LocalDateTime accessTimestamp;
    private List<AccountResponse> accounts;
    private List<TransactionResponse> transactions;
}
