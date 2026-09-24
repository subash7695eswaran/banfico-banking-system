package com.banfico.banking.controller;

import com.banfico.banking.dto.request.CreateTransactionRequest;
import com.banfico.banking.dto.response.ApiResponse;
import com.banfico.banking.dto.response.TransactionResponse;
import com.banfico.banking.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "Deposit, Withdrawal, Transfer and History APIs")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/api/accounts/{accountId}/transactions")
    @Operation(summary = "Execute Transaction on Account", description = "Executes a DEPOSIT, WITHDRAWAL, or TRANSFER for the specified account.")
    public ResponseEntity<ApiResponse<TransactionResponse>> executeTransaction(
            @PathVariable Long accountId,
            @Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse transaction = transactionService.createTransaction(accountId, request);
        return new ResponseEntity<>(ApiResponse.created("Transaction executed successfully", transaction), HttpStatus.CREATED);
    }

    @GetMapping("/api/accounts/{accountId}/transactions")
    @Operation(summary = "Get Account Transaction History", description = "Returns all incoming and outgoing transactions for an account.")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAccountTransactions(@PathVariable Long accountId) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByAccountId(accountId);
        return ResponseEntity.ok(ApiResponse.ok("Transaction history retrieved", transactions));
    }

    @GetMapping("/api/transactions")
    @Operation(summary = "Get all System Transactions", description = "Retrieves a global audit trail of all transactions.")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {
        List<TransactionResponse> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(ApiResponse.ok("All transactions retrieved", transactions));
    }

    @GetMapping("/api/transactions/{reference}")
    @Operation(summary = "Get Transaction by Reference", description = "Retrieves a single transaction by reference.")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByReference(@PathVariable String reference) {
        TransactionResponse transaction = transactionService.getTransactionByReference(reference);
        return ResponseEntity.ok(ApiResponse.ok("Transaction found", transaction));
    }
}
