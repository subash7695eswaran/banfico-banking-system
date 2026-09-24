package com.banfico.banking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Beneficiary name is required")
    @Column(name = "beneficiary_name", nullable = false, length = 100)
    private String beneficiaryName;

    @NotBlank(message = "Beneficiary account number is required")
    @Column(name = "account_number", nullable = false, length = 34)
    private String accountNumber;

    @NotBlank(message = "Bank name is required")
    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
