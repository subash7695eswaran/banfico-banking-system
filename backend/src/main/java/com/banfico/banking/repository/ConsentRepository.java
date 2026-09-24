package com.banfico.banking.repository;

import com.banfico.banking.entity.Consent;
import com.banfico.banking.entity.enums.ConsentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsentRepository extends JpaRepository<Consent, Long> {
    Optional<Consent> findByConsentReference(String consentReference);
    List<Consent> findByCustomerId(Long customerId);
    List<Consent> findByStatus(ConsentStatus status);
}
