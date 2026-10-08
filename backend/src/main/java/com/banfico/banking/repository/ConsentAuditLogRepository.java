package com.banfico.banking.repository;

import com.banfico.banking.entity.ConsentAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsentAuditLogRepository extends JpaRepository<ConsentAuditLog, Long> {
    List<ConsentAuditLog> findByConsentReferenceOrderByCreatedAtDesc(String consentReference);
    List<ConsentAuditLog> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<ConsentAuditLog> findAllByOrderByCreatedAtDesc();
}
