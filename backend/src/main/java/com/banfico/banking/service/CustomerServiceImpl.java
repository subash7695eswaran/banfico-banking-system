package com.banfico.banking.service;

import com.banfico.banking.dto.request.CreateCustomerRequest;
import com.banfico.banking.dto.response.CustomerResponse;
import com.banfico.banking.entity.Customer;
import com.banfico.banking.exception.DuplicateResourceException;
import com.banfico.banking.exception.ResourceNotFoundException;
import com.banfico.banking.mapper.DtoMapper;
import com.banfico.banking.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        log.info("Creating customer with email: {}", request.getEmail());
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Customer with email '" + request.getEmail() + "' already exists");
        }

        Customer customer = Customer.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone().trim())
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Customer successfully created with id: {}", saved.getId());
        return dtoMapper.toCustomerResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        log.info("Fetching all customers");
        return customerRepository.findAll().stream()
                .map(dtoMapper::toCustomerResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        log.info("Fetching customer with id: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return dtoMapper.toCustomerResponse(customer);
    }
}
