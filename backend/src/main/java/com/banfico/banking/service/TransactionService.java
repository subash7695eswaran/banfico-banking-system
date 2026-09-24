package com.banfico.banking.service;

import com.banfico.banking.dto.request.CreateTransactionRequest;
import com.banfico.banking.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {
    TransactionResponse createTransaction(Long accountId, CreateTransactionRequest request);
    List<TransactionResponse> getTransactionsByAccountId(Long accountId);
    List<TransactionResponse> getAllTransactions();
    TransactionResponse getTransactionByReference(String reference);
}
