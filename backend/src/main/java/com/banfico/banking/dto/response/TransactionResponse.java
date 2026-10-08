package com.banfico.banking.dto.response;

import com.banfico.banking.entity.enums.TransactionStatus;
import com.banfico.banking.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String transactionReference;
    private TransactionType transactionType;
    private BigDecimal amount;
    private TransactionStatus status;
    private String description;
    private String currency;
    private String initiatedBy;
    private String approvedBy;
    private String rejectionReason;
    private Long sourceAccountId;
    private String sourceAccountNumber;
    private Long destinationAccountId;
    private String destinationAccountNumber;
    private LocalDateTime createdAt;
}
