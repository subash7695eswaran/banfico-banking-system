package com.banfico.banking.dto.response;

import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private String keycloakSubject;
    private String username;
    private String email;
    private String fullName;
    private String primaryRole;
    private Set<String> roles;
    private Long customerId;
    private List<AccountResponse> accounts;
}
