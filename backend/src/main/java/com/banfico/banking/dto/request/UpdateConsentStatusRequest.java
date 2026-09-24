package com.banfico.banking.dto.request;

import com.banfico.banking.entity.enums.ConsentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateConsentStatusRequest {

    @NotNull(message = "Consent status is required (AUTHORISED, REJECTED, REVOKED, EXPIRED)")
    private ConsentStatus status;
}
