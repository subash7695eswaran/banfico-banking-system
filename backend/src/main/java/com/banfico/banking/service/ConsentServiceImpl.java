package com.banfico.banking.service;

import com.banfico.banking.dto.request.CreateConsentRequest;
import com.banfico.banking.dto.request.RevokeConsentRequest;
import com.banfico.banking.dto.request.UpdateConsentStatusRequest;
import com.banfico.banking.dto.response.ConsentAuditLogResponse;
import com.banfico.banking.dto.response.ConsentResponse;
import com.banfico.banking.entity.BankAccount;
import com.banfico.banking.entity.Consent;
import com.banfico.banking.entity.ConsentAuditLog;
import com.banfico.banking.entity.Customer;
import com.banfico.banking.entity.enums.ConsentStatus;
import com.banfico.banking.exception.ConsentAccessDeniedException;
import com.banfico.banking.exception.InvalidOperationException;
import com.banfico.banking.exception.ResourceNotFoundException;
import com.banfico.banking.mapper.DtoMapper;
import com.banfico.banking.repository.BankAccountRepository;
import com.banfico.banking.repository.ConsentAuditLogRepository;
import com.banfico.banking.repository.ConsentRepository;
import com.banfico.banking.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class ConsentServiceImpl implements ConsentService {

    private final ConsentRepository consentRepository;
    private final CustomerRepository customerRepository;
    private final BankAccountRepository accountRepository;
    private final ConsentAuditLogRepository auditLogRepository;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional
    public ConsentResponse createConsent(CreateConsentRequest request) {
        log.info("Creating consent request for customer id: {}, purpose: {}", request.getCustomerId(), request.getPurpose());
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        BankAccount account = null;
        if (request.getBankAccountId() != null) {
            account = accountRepository.findById(request.getBankAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with id: " + request.getBankAccountId()));
        }

        String consentReference = generateConsentReference();
        LocalDateTime validUntil = request.getValidUntil() != null ? request.getValidUntil() : LocalDateTime.now().plusDays(90);
        String scopes = (request.getScopes() != null && !request.getScopes().trim().isEmpty())
                ? request.getScopes().trim()
                : "READ_ACCOUNTS,READ_BALANCES,READ_TRANSACTIONS";

        Consent consent = Consent.builder()
                .consentReference(consentReference)
                .customer(customer)
                .bankAccount(account)
                .purpose(request.getPurpose().trim())
                .scopes(scopes)
                .tppClientId(request.getTppClientId() != null ? request.getTppClientId().trim() : "tpp-fintech-app")
                .status(ConsentStatus.PENDING)
                .validUntil(validUntil)
                .build();

        Consent saved = consentRepository.save(consent);
        log.info("Consent successfully created with reference: {}", saved.getConsentReference());

        // Audit Trail entry
        auditLogRepository.save(ConsentAuditLog.builder()
                .consentReference(saved.getConsentReference())
                .customerId(customer.getId())
                .action("CONSENT_CREATED")
                .actorRole("CONSENT_REQUESTER")
                .actorIdentifier(saved.getTppClientId())
                .details("Initiated consent for purpose: " + saved.getPurpose() + " with scopes: [" + scopes + "] valid until " + validUntil)
                .build());

        return dtoMapper.toConsentResponse(saved);
    }

    @Override
    @Transactional
    public List<ConsentResponse> getAllConsents() {
        log.info("Fetching all open banking consents");
        List<Consent> consents = consentRepository.findAll();
        consents.forEach(this::checkAndExpireIfNecessary);
        return consents.stream()
                .map(dtoMapper::toConsentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConsentResponse getConsentById(Long id) {
        log.info("Fetching consent with id: {}", id);
        Consent consent = consentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found with id: " + id));
        checkAndExpireIfNecessary(consent);
        return dtoMapper.toConsentResponse(consent);
    }

    @Override
    @Transactional
    public ConsentResponse getConsentByReference(String reference) {
        log.info("Fetching consent with reference: {}", reference);
        Consent consent = consentRepository.findByConsentReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found with reference: " + reference));
        checkAndExpireIfNecessary(consent);
        return dtoMapper.toConsentResponse(consent);
    }

    @Override
    @Transactional
    public Consent getActiveConsentEntityByReference(String reference) {
        Consent consent = consentRepository.findByConsentReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found with reference: " + reference));

        checkAndExpireIfNecessary(consent);

        if (consent.getStatus() != ConsentStatus.AUTHORISED) {
            // Log access denial
            auditLogRepository.save(ConsentAuditLog.builder()
                    .consentReference(reference)
                    .customerId(consent.getCustomer().getId())
                    .action("DATA_ACCESS_DENIED")
                    .actorRole("CONSENT_REQUESTER")
                    .actorIdentifier(consent.getTppClientId())
                    .details("Access denied: Consent status is " + consent.getStatus() + ", must be AUTHORISED")
                    .build());
            throw new ConsentAccessDeniedException("Access Denied: Consent status is " + consent.getStatus() + ". Only AUTHORISED consents permit data access.");
        }

        return consent;
    }

    @Override
    @Transactional
    public List<ConsentResponse> getConsentsByCustomerId(Long customerId) {
        log.info("Fetching consents for customer id: {}", customerId);
        List<Consent> consents = consentRepository.findByCustomerId(customerId);
        consents.forEach(this::checkAndExpireIfNecessary);
        return consents.stream()
                .map(dtoMapper::toConsentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConsentResponse approveConsent(Long id) {
        log.info("Approving consent id: {}", id);
        Consent consent = consentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found with id: " + id));

        if (consent.getStatus() != ConsentStatus.PENDING) {
            throw new InvalidOperationException("Only PENDING consents can be approved. Current status: " + consent.getStatus());
        }

        checkAndExpireIfNecessary(consent);
        if (consent.getStatus() == ConsentStatus.EXPIRED) {
            throw new InvalidOperationException("Cannot approve an EXPIRED consent request.");
        }

        consent.setStatus(ConsentStatus.AUTHORISED);
        Consent updated = consentRepository.save(consent);
        log.info("Consent reference {} approved successfully", updated.getConsentReference());

        // Audit log
        auditLogRepository.save(ConsentAuditLog.builder()
                .consentReference(updated.getConsentReference())
                .customerId(updated.getCustomer().getId())
                .action("CONSENT_AUTHORISED")
                .actorRole("CUSTOMER")
                .actorIdentifier(updated.getCustomer().getEmail())
                .details("Customer authorized TPP [" + updated.getTppClientId() + "] access for scopes: " + updated.getScopes())
                .build());

        return dtoMapper.toConsentResponse(updated);
    }

    @Override
    @Transactional
    public ConsentResponse rejectConsent(Long id) {
        log.info("Rejecting consent id: {}", id);
        Consent consent = consentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found with id: " + id));

        if (consent.getStatus() != ConsentStatus.PENDING) {
            throw new InvalidOperationException("Only PENDING consents can be rejected. Current status: " + consent.getStatus());
        }

        consent.setStatus(ConsentStatus.REJECTED);
        Consent updated = consentRepository.save(consent);
        log.info("Consent reference {} rejected", updated.getConsentReference());

        // Audit log
        auditLogRepository.save(ConsentAuditLog.builder()
                .consentReference(updated.getConsentReference())
                .customerId(updated.getCustomer().getId())
                .action("CONSENT_REJECTED")
                .actorRole("CUSTOMER")
                .actorIdentifier(updated.getCustomer().getEmail())
                .details("Customer rejected TPP [" + updated.getTppClientId() + "] consent request")
                .build());

        return dtoMapper.toConsentResponse(updated);
    }

    @Override
    @Transactional
    public ConsentResponse revokeConsent(Long id, RevokeConsentRequest request) {
        log.info("Revoking consent id: {}", id);
        Consent consent = consentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found with id: " + id));

        if (consent.getStatus() != ConsentStatus.AUTHORISED) {
            throw new InvalidOperationException("Only AUTHORISED consents can be revoked. Current status: " + consent.getStatus());
        }

        consent.setStatus(ConsentStatus.REVOKED);
        consent.setRevokedAt(LocalDateTime.now());
        consent.setRevocationReason(request != null && request.getReason() != null ? request.getReason().trim() : "Customer revoked data access");
        Consent updated = consentRepository.save(consent);
        log.info("Consent reference {} revoked", updated.getConsentReference());

        // Audit log
        String actorId = (request != null && request.getRevokedBy() != null) ? request.getRevokedBy() : updated.getCustomer().getEmail();
        auditLogRepository.save(ConsentAuditLog.builder()
                .consentReference(updated.getConsentReference())
                .customerId(updated.getCustomer().getId())
                .action("CONSENT_REVOKED")
                .actorRole("CUSTOMER")
                .actorIdentifier(actorId)
                .details("Consent revoked: " + updated.getRevocationReason())
                .build());

        return dtoMapper.toConsentResponse(updated);
    }

    @Override
    @Transactional
    public ConsentResponse updateConsentStatus(Long id, UpdateConsentStatusRequest request) {
        log.info("Updating consent id: {} to status: {}", id, request.getStatus());
        Consent consent = consentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found with id: " + id));

        consent.setStatus(request.getStatus());
        Consent updated = consentRepository.save(consent);

        auditLogRepository.save(ConsentAuditLog.builder()
                .consentReference(updated.getConsentReference())
                .customerId(updated.getCustomer().getId())
                .action("STATUS_CHANGED")
                .actorRole("CONSENT_MANAGER")
                .actorIdentifier("consent-manager")
                .details("Consent status updated to " + request.getStatus())
                .build());

        return dtoMapper.toConsentResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsentAuditLogResponse> getConsentAuditLogs(String reference) {
        return auditLogRepository.findByConsentReferenceOrderByCreatedAtDesc(reference).stream()
                .map(dtoMapper::toConsentAuditLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsentAuditLogResponse> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(dtoMapper::toConsentAuditLogResponse)
                .collect(Collectors.toList());
    }

    private void checkAndExpireIfNecessary(Consent consent) {
        if (consent.getStatus() == ConsentStatus.AUTHORISED || consent.getStatus() == ConsentStatus.PENDING) {
            if (consent.getValidUntil() != null && consent.getValidUntil().isBefore(LocalDateTime.now())) {
                consent.setStatus(ConsentStatus.EXPIRED);
                consentRepository.save(consent);
                auditLogRepository.save(ConsentAuditLog.builder()
                        .consentReference(consent.getConsentReference())
                        .customerId(consent.getCustomer().getId())
                        .action("CONSENT_EXPIRED")
                        .actorRole("CONSENT_MANAGER")
                        .actorIdentifier("system-timer")
                        .details("Consent validity period expired on " + consent.getValidUntil())
                        .build());
            }
        }
    }

    private String generateConsentReference() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "CONSENT-" + datePart + "-" + uuidPart;
    }
}
