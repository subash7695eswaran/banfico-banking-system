package com.banfico.banking.dto.request;

import com.banfico.banking.entity.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

    @NotNull(message = "Transaction type is required (DEPOSIT, WITHDRAWAL, TRANSFER)")
    private TransactionType transactionType;

    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be strictly greater than zero")
    private BigDecimal amount;

    private Long destinationAccountId;

    private String description;
}
