package com.banfico.banking.service;

import com.banfico.banking.dto.request.CreateCustomerRequest;
import com.banfico.banking.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {
    CustomerResponse createCustomer(CreateCustomerRequest request);
    List<CustomerResponse> getAllCustomers();
    CustomerResponse getCustomerById(Long id);
}
