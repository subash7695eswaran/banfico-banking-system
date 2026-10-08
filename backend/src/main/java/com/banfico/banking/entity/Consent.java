package com.banfico.banking.entity;

import com.banfico.banking.entity.enums.ConsentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "consents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Consent reference is required")
    @Column(name = "consent_reference", nullable = false, unique = true, length = 64)
    private String consentReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;

    @NotNull(message = "Consent status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ConsentStatus status = ConsentStatus.PENDING;

    @NotBlank(message = "Consent purpose is required")
    @Column(nullable = false, length = 255)
    private String purpose;

    @Column(nullable = false, length = 255)
    @Builder.Default
    private String scopes = "READ_ACCOUNTS,READ_BALANCES,READ_TRANSACTIONS";

    @Column(name = "tpp_client_id", length = 100)
    @Builder.Default
    private String tppClientId = "tpp-fintech-app";

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revocation_reason", length = 255)
    private String revocationReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
