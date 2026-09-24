package com.banfico.banking.controller;

import com.banfico.banking.dto.request.CreateConsentRequest;
import com.banfico.banking.dto.request.UpdateConsentStatusRequest;
import com.banfico.banking.dto.response.ApiResponse;
import com.banfico.banking.dto.response.ConsentResponse;
import com.banfico.banking.service.ConsentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consents")
@RequiredArgsConstructor
@Tag(name = "Open Banking Consent Management", description = "Create, Authorise, and Reject Open Banking Consents (AIS/PIS)")
public class ConsentController {

    private final ConsentService consentService;

    @PostMapping
    @Operation(summary = "Create Open Banking Consent Request", description = "Third Party Provider initiates an Account Information or Payment Initiation consent.")
    public ResponseEntity<ApiResponse<ConsentResponse>> createConsent(@Valid @RequestBody CreateConsentRequest request) {
        ConsentResponse consent = consentService.createConsent(request);
        return new ResponseEntity<>(ApiResponse.created("Consent request created successfully", consent), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all Consents", description = "Retrieves all open banking consent records.")
    public ResponseEntity<ApiResponse<List<ConsentResponse>>> getAllConsents() {
        List<ConsentResponse> consents = consentService.getAllConsents();
        return ResponseEntity.ok(ApiResponse.ok("Consents retrieved successfully", consents));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Consent by ID", description = "Retrieves consent details and authorisation status.")
    public ResponseEntity<ApiResponse<ConsentResponse>> getConsentById(@PathVariable Long id) {
        ConsentResponse consent = consentService.getConsentById(id);
        return ResponseEntity.ok(ApiResponse.ok("Consent found", consent));
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get Consent by Reference", description = "Retrieves consent details by unique reference number.")
    public ResponseEntity<ApiResponse<ConsentResponse>> getConsentByReference(@PathVariable String reference) {
        ConsentResponse consent = consentService.getConsentByReference(reference);
        return ResponseEntity.ok(ApiResponse.ok("Consent found", consent));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get Consents by Customer", description = "Retrieves all consents granted or pending for a specific customer.")
    public ResponseEntity<ApiResponse<List<ConsentResponse>>> getConsentsByCustomer(@PathVariable Long customerId) {
        List<ConsentResponse> consents = consentService.getConsentsByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.ok("Customer consents retrieved", consents));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve / Authorise Consent", description = "Customer authorises the TPP to access account details or initiate payment.")
    public ResponseEntity<ApiResponse<ConsentResponse>> approveConsent(@PathVariable Long id) {
        ConsentResponse consent = consentService.approveConsent(id);
        return ResponseEntity.ok(ApiResponse.ok("Consent successfully AUTHORISED", consent));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject Consent", description = "Customer or Bank rejects the TPP consent request.")
    public ResponseEntity<ApiResponse<ConsentResponse>> rejectConsent(@PathVariable Long id) {
        ConsentResponse consent = consentService.rejectConsent(id);
        return ResponseEntity.ok(ApiResponse.ok("Consent marked as REJECTED", consent));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update Consent Status", description = "Directly update consent state (AUTHORISED, REJECTED, REVOKED, EXPIRED).")
    public ResponseEntity<ApiResponse<ConsentResponse>> updateConsentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateConsentStatusRequest request) {
        ConsentResponse consent = consentService.updateConsentStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Consent status updated", consent));
    }
}
