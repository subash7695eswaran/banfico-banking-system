package com.banfico.banking.controller;

import com.banfico.banking.dto.request.CreateAccountRequest;
import com.banfico.banking.dto.response.AccountResponse;
import com.banfico.banking.dto.response.ApiResponse;
import com.banfico.banking.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse account = accountService.createAccount(request);
        return new ResponseEntity<>(ApiResponse.created("Bank account created successfully", account), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {
        List<AccountResponse> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(ApiResponse.ok("Bank accounts retrieved successfully", accounts));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable Long accountId) {
        AccountResponse account = accountService.getAccountById(accountId);
        return ResponseEntity.ok(ApiResponse.ok("Bank account found", account));
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(@PathVariable String accountNumber) {
        AccountResponse account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.ok("Bank account found", account));
    }
}
