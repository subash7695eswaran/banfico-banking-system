package com.banfico.banking.dto.request;

import com.banfico.banking.entity.enums.AccountType;
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
public class CreateAccountRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Account type is required (SAVINGS, CURRENT, CHECKING)")
    private AccountType accountType;

    @DecimalMin(value = "0.0", inclusive = true, message = "Initial deposit cannot be negative")
    @Builder.Default
    private BigDecimal initialDeposit = BigDecimal.ZERO;
}
