package com.banfico.banking.service;

import com.banfico.banking.dto.request.CreateConsentRequest;
import com.banfico.banking.dto.request.UpdateConsentStatusRequest;
import com.banfico.banking.dto.response.ConsentResponse;
import com.banfico.banking.entity.BankAccount;
import com.banfico.banking.entity.Consent;
import com.banfico.banking.entity.Customer;
import com.banfico.banking.entity.enums.ConsentStatus;
import com.banfico.banking.exception.InvalidOperationException;
import com.banfico.banking.exception.ResourceNotFoundException;
import com.banfico.banking.mapper.DtoMapper;
import com.banfico.banking.repository.BankAccountRepository;
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
public class ConsentServiceImpl implements ConsentService {

    private final ConsentRepository consentRepository;
    private final CustomerRepository customerRepository;
    private final BankAccountRepository accountRepository;
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

        Consent consent = Consent.builder()
                .consentReference(consentReference)
                .customer(customer)
                .bankAccount(account)
                .purpose(request.getPurpose().trim())
                .tppClientId(request.getTppClientId() != null ? request.getTppClientId().trim() : "tpp-fintech-app")
                .status(ConsentStatus.PENDING)
                .validUntil(validUntil)
                .build();

        Consent saved = consentRepository.save(consent);
        log.info("Consent successfully created with reference: {}", saved.getConsentReference());
        return dtoMapper.toConsentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsentResponse> getAllConsents() {
        log.info("Fetching all open banking consents");
        return consentRepository.findAll().stream()
                .map(dtoMapper::toConsentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConsentResponse getConsentById(Long id) {
        log.info("Fetching consent with id: {}", id);
        Consent consent = consentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found with id: " + id));
        return dtoMapper.toConsentResponse(consent);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsentResponse getConsentByReference(String reference) {
        log.info("Fetching consent with reference: {}", reference);
        Consent consent = consentRepository.findByConsentReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found with reference: " + reference));
        return dtoMapper.toConsentResponse(consent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsentResponse> getConsentsByCustomerId(Long customerId) {
        log.info("Fetching consents for customer id: {}", customerId);
        return consentRepository.findByCustomerId(customerId).stream()
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

        consent.setStatus(ConsentStatus.AUTHORISED);
        Consent updated = consentRepository.save(consent);
        log.info("Consent reference {} approved successfully", updated.getConsentReference());
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
        return dtoMapper.toConsentResponse(updated);
    }

    private String generateConsentReference() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "CONSENT-" + datePart + "-" + uuidPart;
    }
}
