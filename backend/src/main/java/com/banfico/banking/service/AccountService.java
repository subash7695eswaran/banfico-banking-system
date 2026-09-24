package com.banfico.banking.service;

import com.banfico.banking.dto.request.CreateAccountRequest;
import com.banfico.banking.dto.response.AccountResponse;

import java.util.List;

public interface AccountService {
    AccountResponse createAccount(CreateAccountRequest request);
    List<AccountResponse> getAllAccounts();
    AccountResponse getAccountById(Long accountId);
    AccountResponse getAccountByNumber(String accountNumber);
    List<AccountResponse> getAccountsByCustomerId(Long customerId);
}
