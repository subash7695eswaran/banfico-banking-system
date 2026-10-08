package com.banfico.banking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "consent_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "consent_reference", nullable = false, length = 64)
    private String consentReference;

    @NotNull
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String action;

    @NotBlank
    @Column(name = "actor_role", nullable = false, length = 50)
    private String actorRole;

    @NotBlank
    @Column(name = "actor_identifier", nullable = false, length = 100)
    private String actorIdentifier;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
