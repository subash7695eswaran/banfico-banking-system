package com.banfico.banking.repository;

import com.banfico.banking.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByEmailIgnoreCase(String email);
    Optional<Customer> findByKeycloakUserId(String keycloakUserId);
    boolean existsByEmail(String email);
    boolean existsByKeycloakUserId(String keycloakUserId);
}
