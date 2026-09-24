package com.banfico.banking.service;

import com.banfico.banking.dto.request.CreateConsentRequest;
import com.banfico.banking.dto.request.UpdateConsentStatusRequest;
import com.banfico.banking.dto.response.ConsentResponse;
import com.banfico.banking.entity.enums.ConsentStatus;

import java.util.List;

public interface ConsentService {
    ConsentResponse createConsent(CreateConsentRequest request);
    List<ConsentResponse> getAllConsents();
    ConsentResponse getConsentById(Long id);
    ConsentResponse getConsentByReference(String reference);
    List<ConsentResponse> getConsentsByCustomerId(Long customerId);
    ConsentResponse approveConsent(Long id);
    ConsentResponse rejectConsent(Long id);
    ConsentResponse updateConsentStatus(Long id, UpdateConsentStatusRequest request);
}
