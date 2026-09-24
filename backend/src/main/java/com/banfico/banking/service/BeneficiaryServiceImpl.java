package com.banfico.banking.service;

import com.banfico.banking.dto.request.CreateBeneficiaryRequest;
import com.banfico.banking.dto.response.BeneficiaryResponse;
import com.banfico.banking.entity.Beneficiary;
import com.banfico.banking.entity.Customer;
import com.banfico.banking.exception.ResourceNotFoundException;
import com.banfico.banking.mapper.DtoMapper;
import com.banfico.banking.repository.BeneficiaryRepository;
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
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional
    public BeneficiaryResponse createBeneficiary(CreateBeneficiaryRequest request) {
        log.info("Creating beneficiary: {} for customer id: {}", request.getBeneficiaryName(), request.getCustomerId());
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        Beneficiary beneficiary = Beneficiary.builder()
                .beneficiaryName(request.getBeneficiaryName().trim())
                .accountNumber(request.getAccountNumber().trim())
                .bankName(request.getBankName().trim())
                .customer(customer)
                .build();

        Beneficiary saved = beneficiaryRepository.save(beneficiary);
        return dtoMapper.toBeneficiaryResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getAllBeneficiaries() {
        log.info("Fetching all beneficiaries");
        return beneficiaryRepository.findAll().stream()
                .map(dtoMapper::toBeneficiaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getBeneficiariesByCustomerId(Long customerId) {
        log.info("Fetching beneficiaries for customer id: {}", customerId);
        return beneficiaryRepository.findByCustomerId(customerId).stream()
                .map(dtoMapper::toBeneficiaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBeneficiary(Long id) {
        log.info("Deleting beneficiary with id: {}", id);
        if (!beneficiaryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Beneficiary not found with id: " + id);
        }
        beneficiaryRepository.deleteById(id);
    }
}
