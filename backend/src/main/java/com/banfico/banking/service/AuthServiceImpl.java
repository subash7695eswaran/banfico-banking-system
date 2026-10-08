package com.banfico.banking.service;

import com.banfico.banking.dto.request.LoginRequest;
import com.banfico.banking.dto.request.RegisterRequest;
import com.banfico.banking.dto.response.AccountResponse;
import com.banfico.banking.dto.response.AuthResponse;
import com.banfico.banking.dto.response.UserProfileResponse;
import com.banfico.banking.entity.BankAccount;
import com.banfico.banking.entity.ConsentAuditLog;
import com.banfico.banking.entity.Customer;
import com.banfico.banking.entity.enums.AccountStatus;
import com.banfico.banking.entity.enums.AccountType;
import com.banfico.banking.exception.DuplicateResourceException;
import com.banfico.banking.exception.InvalidOperationException;
import com.banfico.banking.mapper.DtoMapper;
import com.banfico.banking.repository.BankAccountRepository;
import com.banfico.banking.repository.ConsentAuditLogRepository;
import com.banfico.banking.repository.CustomerRepository;
import com.banfico.banking.security.SecurityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository customerRepository;
    private final BankAccountRepository accountRepository;
    private final ConsentAuditLogRepository auditLogRepository;
    private final DtoMapper dtoMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:8081/realms/banfico-realm}")
    private String keycloakIssuerUri;

    @Value("${KEYCLOAK_AUTH_SERVER_URL:http://localhost:8081}")
    private String keycloakBaseUrl;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating user {} via Keycloak IAM", request.getUsername());

        String tokenUrl = keycloakIssuerUri + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "banfico-banking-app");
        body.add("username", request.getUsername().trim());
        body.add("password", request.getPassword());
        body.add("scope", "openid profile email");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                String accessToken = json.path("access_token").asText();
                String refreshToken = json.path("refresh_token").asText();
                String idToken = json.path("id_token").asText();
                long expiresIn = json.path("expires_in").asLong(300L);

                // Parse JWT payload claims
                JsonNode payload = parseJwtPayload(accessToken);
                String sub = payload.path("sub").asText();
                String username = payload.path("preferred_username").asText(request.getUsername());
                String email = payload.path("email").asText();
                String name = payload.path("name").asText(username);

                Set<String> roles = extractRoles(payload);
                String primaryRole = determinePrimaryRole(roles);

                Long customerId = null;
                if ("CUSTOMER".equalsIgnoreCase(primaryRole)) {
                    Customer customer = provisionOrGetCustomer(sub, email, name, username);
                    customerId = customer.getId();
                }

                // Log authentication audit event
                auditLogRepository.save(ConsentAuditLog.builder()
                        .consentReference("AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .customerId(customerId != null ? customerId : 1L)
                        .action("USER_AUTHENTICATED")
                        .actorRole(primaryRole)
                        .actorIdentifier(email != null && !email.isEmpty() ? email : username)
                        .details("Successfully authenticated via Keycloak OpenID Connect. Detected Role: " + primaryRole)
                        .build());

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .idToken(idToken)
                        .tokenType("Bearer")
                        .expiresIn(expiresIn)
                        .username(username)
                        .email(email)
                        .fullName(name)
                        .primaryRole(primaryRole)
                        .roles(roles)
                        .customerId(customerId)
                        .build();
            }
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest e) {
            log.warn("Keycloak authentication failed for user {}: {}", request.getUsername(), e.getResponseBodyAsString());
            throw new InvalidOperationException("Invalid Keycloak credentials. Please verify your username and password.");
        } catch (Exception e) {
            log.error("Error communicating with Keycloak token endpoint: {}", e.getMessage(), e);
            throw new InvalidOperationException("Keycloak IAM Service is temporarily unavailable. (" + e.getMessage() + ")");
        }

        throw new InvalidOperationException("Authentication failed: Unable to obtain access token from Keycloak.");
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing self-registration for new customer: {} ({})", request.getUsername(), request.getEmail());

        if (customerRepository.existsByEmail(request.getEmail().trim())) {
            throw new DuplicateResourceException("A customer with email " + request.getEmail() + " already exists");
        }

        // 1. Register user directly into Keycloak via Keycloak Admin API or Direct Registration
        String keycloakUserId = registerInKeycloak(request);

        // 2. Provision Customer record with Keycloak Subject mapping
        String fullName = (request.getFirstName().trim() + " " + request.getLastName().trim()).trim();
        Customer customer = Customer.builder()
                .name(fullName)
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone() != null && !request.getPhone().trim().isEmpty() ? request.getPhone().trim() : "+91 98765 43210")
                .keycloakUserId(keycloakUserId)
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        // 3. Create initial SAVINGS bank account with ₹25,000 INR balance
        String accountNumber = "IN" + (1000000000L + new SecureRandom().nextInt(900000000));
        BankAccount initialAccount = BankAccount.builder()
                .accountNumber(accountNumber)
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("25000.00"))
                .status(AccountStatus.ACTIVE)
                .customer(savedCustomer)
                .build();
        accountRepository.save(initialAccount);

        // 4. Log Registration Audit Event
        auditLogRepository.save(ConsentAuditLog.builder()
                .consentReference("REG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customerId(savedCustomer.getId())
                .action("USER_REGISTERED")
                .actorRole("CUSTOMER")
                .actorIdentifier(savedCustomer.getEmail())
                .details("New customer self-registered via Keycloak IAM. Assigned role: CUSTOMER. Initial account: " + accountNumber)
                .build());

        // 5. Automatically log the newly registered user in via Keycloak
        return login(LoginRequest.builder()
                .username(request.getUsername().trim())
                .password(request.getPassword())
                .build());
    }

    @Override
    @Transactional
    public UserProfileResponse getCurrentUserProfile() {
        String username = SecurityUtils.getCurrentUser();
        String email = SecurityUtils.getCurrentEmail();
        String sub = SecurityUtils.getCurrentKeycloakSubject();
        String name = SecurityUtils.getCurrentFullName();
        Set<String> roles = SecurityUtils.getCurrentRoles();
        String primaryRole = determinePrimaryRole(roles);

        Long customerId = null;
        List<AccountResponse> accounts = Collections.emptyList();

        if ("CUSTOMER".equalsIgnoreCase(primaryRole)) {
            Customer customer = provisionOrGetCustomer(sub, email, name, username);
            customerId = customer.getId();
            accounts = accountRepository.findByCustomerId(customerId).stream()
                    .map(dtoMapper::toAccountResponse)
                    .collect(Collectors.toList());
        }

        return UserProfileResponse.builder()
                .keycloakSubject(sub)
                .username(username)
                .email(email)
                .fullName(name)
                .primaryRole(primaryRole)
                .roles(roles)
                .customerId(customerId)
                .accounts(accounts)
                .build();
    }

    private Customer provisionOrGetCustomer(String keycloakUserId, String email, String name, String username) {
        if (keycloakUserId != null) {
            Optional<Customer> existing = customerRepository.findByKeycloakUserId(keycloakUserId);
            if (existing.isPresent()) return existing.get();
        }

        if (email != null && !email.trim().isEmpty()) {
            Optional<Customer> existing = customerRepository.findByEmailIgnoreCase(email.trim());
            if (existing.isPresent()) {
                Customer c = existing.get();
                if (c.getKeycloakUserId() == null && keycloakUserId != null) {
                    c.setKeycloakUserId(keycloakUserId);
                    return customerRepository.save(c);
                }
                return c;
            }
        }

        // Auto-provision new customer profile for first-time Keycloak login
        log.info("Auto-provisioning customer profile for Keycloak user: {} ({})", username, email);
        String effectiveName = (name != null && !name.trim().isEmpty()) ? name.trim() : username;
        String effectiveEmail = (email != null && !email.trim().isEmpty()) ? email.trim() : username + "@banfico.com";

        Customer newCustomer = Customer.builder()
                .name(effectiveName)
                .email(effectiveEmail)
                .phone("+91 98765 " + (10000 + new SecureRandom().nextInt(90000)))
                .keycloakUserId(keycloakUserId)
                .build();

        Customer saved = customerRepository.save(newCustomer);

        // Create default savings account
        String accountNumber = "IN" + (1000000000L + new SecureRandom().nextInt(900000000));
        BankAccount acc = BankAccount.builder()
                .accountNumber(accountNumber)
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("25000.00"))
                .status(AccountStatus.ACTIVE)
                .customer(saved)
                .build();
        accountRepository.save(acc);

        return saved;
    }

    private String registerInKeycloak(RegisterRequest request) {
        try {
            // Obtain master admin token from Keycloak
            String tokenUrl = keycloakBaseUrl + "/realms/master/protocol/openid-connect/token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", "admin-cli");
            body.add("username", "admin");
            body.add("password", "admin123");

            ResponseEntity<String> tokenRes = restTemplate.postForEntity(tokenUrl, new HttpEntity<>(body, headers), String.class);
            if (tokenRes.getStatusCode().is2xxSuccessful() && tokenRes.getBody() != null) {
                String adminToken = objectMapper.readTree(tokenRes.getBody()).path("access_token").asText();

                // Create User in Keycloak banfico-realm
                String createUserUrl = keycloakBaseUrl + "/admin/realms/banfico-realm/users";
                HttpHeaders userHeaders = new HttpHeaders();
                userHeaders.setContentType(MediaType.APPLICATION_JSON);
                userHeaders.setBearerAuth(adminToken);

                Map<String, Object> userPayload = new HashMap<>();
                userPayload.put("username", request.getUsername().trim());
                userPayload.put("email", request.getEmail().trim());
                userPayload.put("firstName", request.getFirstName().trim());
                userPayload.put("lastName", request.getLastName().trim());
                userPayload.put("enabled", true);
                userPayload.put("emailVerified", true);

                Map<String, Object> cred = new HashMap<>();
                cred.put("type", "password");
                cred.put("value", request.getPassword());
                cred.put("temporary", false);
                userPayload.put("credentials", List.of(cred));

                userPayload.put("realmRoles", List.of("customer"));

                ResponseEntity<String> createRes = restTemplate.postForEntity(createUserUrl, new HttpEntity<>(userPayload, userHeaders), String.class);
                if (createRes.getStatusCode() == HttpStatus.CREATED) {
                    // Extract user ID from Location header
                    String location = createRes.getHeaders().getFirst("Location");
                    if (location != null) {
                        return location.substring(location.lastIndexOf('/') + 1);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not register user via Keycloak Admin REST API (Fallback to local auto-provision): {}", e.getMessage());
        }
        return UUID.randomUUID().toString();
    }

    private JsonNode parseJwtPayload(String jwtToken) throws Exception {
        String[] parts = jwtToken.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT token structure");
        }
        byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
        return objectMapper.readTree(new String(decoded, StandardCharsets.UTF_8));
    }

    private Set<String> extractRoles(JsonNode payload) {
        Set<String> roles = new HashSet<>();
        JsonNode realmAccess = payload.path("realm_access").path("roles");
        if (realmAccess.isArray()) {
            for (JsonNode r : realmAccess) {
                roles.add(r.asText().toUpperCase());
            }
        }
        return roles;
    }

    private String determinePrimaryRole(Set<String> roles) {
        if (roles.contains("ADMIN")) return "ADMIN";
        if (roles.contains("CHECKER")) return "CHECKER";
        if (roles.contains("MAKER")) return "MAKER";
        if (roles.contains("CONSENT_MANAGER")) return "CONSENT_MANAGER";
        if (roles.contains("CONSENT_REQUESTER") || roles.contains("TPP")) return "CONSENT_REQUESTER";
        return "CUSTOMER";
    }
}
