package com.banfico.banking.controller;

import com.banfico.banking.dto.request.CreateCustomerRequest;
import com.banfico.banking.dto.response.AccountResponse;
import com.banfico.banking.dto.response.ApiResponse;
import com.banfico.banking.dto.response.ConsentResponse;
import com.banfico.banking.dto.response.CustomerResponse;
import com.banfico.banking.service.AccountService;
import com.banfico.banking.service.ConsentService;
import com.banfico.banking.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final AccountService accountService;
    private final ConsentService consentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse customer = customerService.createCustomer(request);
        return new ResponseEntity<>(ApiResponse.created("Customer created successfully", customer), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        List<CustomerResponse> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.ok("Customers retrieved successfully", customers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Long id) {
        CustomerResponse customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer found", customer));
    }

    @GetMapping("/{id}/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getCustomerAccounts(@PathVariable Long id) {
        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer accounts retrieved", accounts));
    }

    @GetMapping("/{id}/consents")
    public ResponseEntity<ApiResponse<List<ConsentResponse>>> getCustomerConsents(@PathVariable Long id) {
        List<ConsentResponse> consents = consentService.getConsentsByCustomerId(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer consents retrieved", consents));
    }
}
