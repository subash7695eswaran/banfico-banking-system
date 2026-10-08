package com.banfico.banking.service;

import com.banfico.banking.dto.request.CreateTransactionRequest;
import com.banfico.banking.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {
    TransactionResponse createTransaction(Long accountId, CreateTransactionRequest request);
    List<TransactionResponse> getTransactionsByAccountId(Long accountId);
    List<TransactionResponse> getAllTransactions();
    List<TransactionResponse> getPendingTransactions();
    TransactionResponse getTransactionByReference(String reference);
    TransactionResponse approveTransaction(Long id, String checkerName);
    TransactionResponse rejectTransaction(Long id, String reason);
}
