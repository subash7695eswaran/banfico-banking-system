package com.banfico.banking.controller;

import com.banfico.banking.dto.request.CreateCustomerRequest;
import com.banfico.banking.dto.response.AccountResponse;
import com.banfico.banking.dto.response.ApiResponse;
import com.banfico.banking.dto.response.ConsentResponse;
import com.banfico.banking.dto.response.CustomerResponse;
import com.banfico.banking.service.AccountService;
import com.banfico.banking.service.ConsentService;
import com.banfico.banking.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Customer onboarding and retrieval APIs")
public class CustomerController {

    private final CustomerService customerService;
    private final AccountService accountService;
    private final ConsentService consentService;

    @PostMapping
    @Operation(summary = "Create a new Customer", description = "Registers a new bank customer with name, email, and phone.")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse customer = customerService.createCustomer(request);
        return new ResponseEntity<>(ApiResponse.created("Customer created successfully", customer), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all Customers", description = "Returns a list of all registered bank customers.")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        List<CustomerResponse> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.ok("Customers retrieved successfully", customers));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Customer by ID", description = "Returns details of a specific customer by ID.")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Long id) {
        CustomerResponse customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer found", customer));
    }

    @GetMapping("/{id}/accounts")
    @Operation(summary = "Get Customer Accounts", description = "Retrieves all bank accounts linked to a specific customer.")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getCustomerAccounts(@PathVariable Long id) {
        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer accounts retrieved", accounts));
    }

    @GetMapping("/{id}/consents")
    @Operation(summary = "Get Customer Open Banking Consents", description = "Retrieves all open banking consent records for a customer.")
    public ResponseEntity<ApiResponse<List<ConsentResponse>>> getCustomerConsents(@PathVariable Long id) {
        List<ConsentResponse> consents = consentService.getConsentsByCustomerId(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer consents retrieved", consents));
    }
}
