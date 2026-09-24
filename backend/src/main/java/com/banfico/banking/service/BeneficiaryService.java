package com.banfico.banking.service;

import com.banfico.banking.dto.request.CreateBeneficiaryRequest;
import com.banfico.banking.dto.response.BeneficiaryResponse;

import java.util.List;

public interface BeneficiaryService {
    BeneficiaryResponse createBeneficiary(CreateBeneficiaryRequest request);
    List<BeneficiaryResponse> getAllBeneficiaries();
    List<BeneficiaryResponse> getBeneficiariesByCustomerId(Long customerId);
    void deleteBeneficiary(Long id);
}
