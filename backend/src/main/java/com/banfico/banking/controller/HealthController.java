package com.banfico.banking.controller;

import com.banfico.banking.dto.response.ApiResponse;
import com.banfico.banking.dto.response.HealthResponse;
import com.banfico.banking.dto.response.InfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;
    private final Environment environment;

    @Value("${app.name:Banfico Mini Banking System}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:Open Banking Consent Management & Core Banking Platform}")
    private String appDescription;

    @Value("${app.program:Banfico University Full-Stack Training}")
    private String programName;

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        String dbStatus = "UNKNOWN";
        Map<String, Object> details = new LinkedHashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                dbStatus = "CONNECTED";
                details.put("databaseProduct", conn.getMetaData().getDatabaseProductName());
                details.put("databaseVersion", conn.getMetaData().getDatabaseProductVersion());
            }
        } catch (Exception e) {
            dbStatus = "DISCONNECTED (" + e.getMessage() + ")";
        }

        details.put("activeProfiles", Arrays.toString(environment.getActiveProfiles()));
        details.put("uptime", System.currentTimeMillis());

        HealthResponse health = HealthResponse.builder()
                .status("UP")
                .service(appName)
                .timestamp(LocalDateTime.now())
                .database(dbStatus)
                .details(details)
                .build();

        return ResponseEntity.ok(health);
    }

    @GetMapping("/api/info")
    public ResponseEntity<ApiResponse<InfoResponse>> getInfo() {
        Map<String, String> keyEndpoints = new LinkedHashMap<>();
        keyEndpoints.put("Health", "GET /health");
        keyEndpoints.put("Info", "GET /api/info");
        keyEndpoints.put("Customers", "GET /api/customers, POST /api/customers");
        keyEndpoints.put("Accounts", "GET /api/accounts, POST /api/accounts");
        keyEndpoints.put("Transactions", "POST /api/accounts/{id}/transactions, GET /api/accounts/{id}/transactions");
        keyEndpoints.put("Beneficiaries", "GET /api/beneficiaries, POST /api/beneficiaries, DELETE /api/beneficiaries/{id}");
        keyEndpoints.put("Consents", "POST /api/consents, GET /api/consents, POST /api/consents/{id}/approve, POST /api/consents/{id}/reject");

        List<String> modules = List.of(
                "Week 1: Setup & Health Check APIs",
                "Week 2: Core Banking CRUD (Customers, Accounts, Transactions, Beneficiaries)",
                "Week 3: React Frontend Integration",
                "Week 4: Keycloak Identity & Role-Based Access Control",
                "Week 5: Nginx API Gateway & Docker Compose Orchestration",
                "Week 6: Open Banking Consent Lifecycle Management"
        );

        InfoResponse info = InfoResponse.builder()
                .application(appName)
                .version(appVersion)
                .description(appDescription)
                .program(programName)
                .javaVersion(System.getProperty("java.version"))
                .springBootVersion("3.3.4")
                .environment(String.join(",", environment.getActiveProfiles()))
                .availableModules(modules)
                .keyEndpoints(keyEndpoints)
                .build();

        return ResponseEntity.ok(ApiResponse.ok("Application info retrieved successfully", info));
    }
}
