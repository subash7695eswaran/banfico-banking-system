package com.banfico.banking.service;

import com.banfico.banking.dto.request.CreateConsentRequest;
import com.banfico.banking.dto.request.RevokeConsentRequest;
import com.banfico.banking.dto.request.UpdateConsentStatusRequest;
import com.banfico.banking.dto.response.ConsentAuditLogResponse;
import com.banfico.banking.dto.response.ConsentResponse;
import com.banfico.banking.entity.Consent;

import java.util.List;

public interface ConsentService {
    ConsentResponse createConsent(CreateConsentRequest request);
    List<ConsentResponse> getAllConsents();
    ConsentResponse getConsentById(Long id);
    ConsentResponse getConsentByReference(String reference);
    Consent getActiveConsentEntityByReference(String reference);
    List<ConsentResponse> getConsentsByCustomerId(Long customerId);
    ConsentResponse approveConsent(Long id);
    ConsentResponse rejectConsent(Long id);
    ConsentResponse revokeConsent(Long id, RevokeConsentRequest request);
    ConsentResponse updateConsentStatus(Long id, UpdateConsentStatusRequest request);
    List<ConsentAuditLogResponse> getConsentAuditLogs(String reference);
    List<ConsentAuditLogResponse> getAllAuditLogs();
}
