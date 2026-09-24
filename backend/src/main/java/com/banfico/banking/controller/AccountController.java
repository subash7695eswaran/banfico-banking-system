package com.banfico.banking.controller;

import com.banfico.banking.dto.request.CreateAccountRequest;
import com.banfico.banking.dto.response.AccountResponse;
import com.banfico.banking.dto.response.ApiResponse;
import com.banfico.banking.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Bank Account Management", description = "Account creation, balance lookup, and lifecycle APIs")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Open a Bank Account", description = "Creates a new bank account with IBAN for an existing customer.")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse account = accountService.createAccount(request);
        return new ResponseEntity<>(ApiResponse.created("Bank account created successfully", account), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all Accounts", description = "Returns a list of all bank accounts across all customers.")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {
        List<AccountResponse> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(ApiResponse.ok("Bank accounts retrieved successfully", accounts));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Get Account by ID", description = "Retrieves details of a bank account by ID.")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable Long accountId) {
        AccountResponse account = accountService.getAccountById(accountId);
        return ResponseEntity.ok(ApiResponse.ok("Bank account found", account));
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Get Account by Account Number", description = "Retrieves account by IBAN / account number.")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(@PathVariable String accountNumber) {
        AccountResponse account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.ok("Bank account found", account));
    }
}
