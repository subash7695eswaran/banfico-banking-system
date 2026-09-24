package com.banfico.banking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfoResponse {
    private String application;
    private String version;
    private String description;
    private String program;
    private String javaVersion;
    private String springBootVersion;
    private String environment;
    private List<String> availableModules;
    private Map<String, String> keyEndpoints;
}
