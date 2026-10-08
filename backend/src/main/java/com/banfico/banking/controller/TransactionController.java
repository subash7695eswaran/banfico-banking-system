package com.banfico.banking.controller;

import com.banfico.banking.dto.request.CreateTransactionRequest;
import com.banfico.banking.dto.response.ApiResponse;
import com.banfico.banking.dto.response.TransactionResponse;
import com.banfico.banking.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/api/accounts/{accountId}/transactions")
    public ResponseEntity<ApiResponse<TransactionResponse>> executeTransaction(
            @PathVariable Long accountId,
            @Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse transaction = transactionService.createTransaction(accountId, request);
        return new ResponseEntity<>(ApiResponse.created("Transaction executed successfully", transaction), HttpStatus.CREATED);
    }

    @GetMapping("/api/accounts/{accountId}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAccountTransactions(@PathVariable Long accountId) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByAccountId(accountId);
        return ResponseEntity.ok(ApiResponse.ok("Transaction history retrieved", transactions));
    }

    @GetMapping("/api/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions() {
        List<TransactionResponse> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(ApiResponse.ok("All transactions retrieved", transactions));
    }

    @GetMapping("/api/transactions/pending")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getPendingTransactions() {
        List<TransactionResponse> pending = transactionService.getPendingTransactions();
        return ResponseEntity.ok(ApiResponse.ok("Pending approval transactions retrieved", pending));
    }

    @PostMapping("/api/transactions/{id}/approve")
    public ResponseEntity<ApiResponse<TransactionResponse>> approveTransaction(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Checker") String checkerName) {
        TransactionResponse tx = transactionService.approveTransaction(id, checkerName);
        return ResponseEntity.ok(ApiResponse.ok("Transaction approved and executed", tx));
    }

    @PostMapping("/api/transactions/{id}/reject")
    public ResponseEntity<ApiResponse<TransactionResponse>> rejectTransaction(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Declined by Approver") String reason) {
        TransactionResponse tx = transactionService.rejectTransaction(id, reason);
        return ResponseEntity.ok(ApiResponse.ok("Transaction rejected", tx));
    }

    @GetMapping("/api/transactions/{reference}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByReference(@PathVariable String reference) {
        TransactionResponse transaction = transactionService.getTransactionByReference(reference);
        return ResponseEntity.ok(ApiResponse.ok("Transaction found", transaction));
    }
}
