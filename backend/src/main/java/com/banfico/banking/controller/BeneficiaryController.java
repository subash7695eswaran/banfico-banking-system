package com.banfico.banking.controller;

import com.banfico.banking.dto.request.CreateBeneficiaryRequest;
import com.banfico.banking.dto.response.ApiResponse;
import com.banfico.banking.dto.response.BeneficiaryResponse;
import com.banfico.banking.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> addBeneficiary(@Valid @RequestBody CreateBeneficiaryRequest request) {
        BeneficiaryResponse beneficiary = beneficiaryService.createBeneficiary(request);
        return new ResponseEntity<>(ApiResponse.created("Beneficiary added successfully", beneficiary), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getAllBeneficiaries() {
        List<BeneficiaryResponse> beneficiaries = beneficiaryService.getAllBeneficiaries();
        return ResponseEntity.ok(ApiResponse.ok("Beneficiaries retrieved successfully", beneficiaries));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getBeneficiariesByCustomer(@PathVariable Long customerId) {
        List<BeneficiaryResponse> beneficiaries = beneficiaryService.getBeneficiariesByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.ok("Customer beneficiaries retrieved", beneficiaries));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBeneficiary(@PathVariable Long id) {
        beneficiaryService.deleteBeneficiary(id);
        return ResponseEntity.ok(ApiResponse.ok("Beneficiary removed successfully", null));
    }
}
