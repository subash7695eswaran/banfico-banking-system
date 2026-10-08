package com.banfico.banking.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SecurityUtils {

    public static String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            if (auth.getPrincipal() instanceof Jwt jwt) {
                String username = jwt.getClaimAsString("preferred_username");
                if (username != null && !username.trim().isEmpty()) {
                    return username;
                }
                String email = jwt.getClaimAsString("email");
                if (email != null && !email.trim().isEmpty()) {
                    return email;
                }
                return jwt.getSubject();
            }
            return auth.getName();
        }

        // Check request headers (for proxy / test clients)
        HttpServletRequest request = getCurrentHttpRequest();
        if (request != null) {
            String headerUser = request.getHeader("X-Authenticated-User");
            if (headerUser != null && !headerUser.trim().isEmpty()) {
                return headerUser.trim();
            }
        }

        return "system";
    }

    public static String getCurrentKeycloakSubject() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        HttpServletRequest request = getCurrentHttpRequest();
        if (request != null) {
            String headerSub = request.getHeader("X-Keycloak-Subject");
            if (headerSub != null && !headerSub.trim().isEmpty()) {
                return headerSub.trim();
            }
        }
        return null;
    }

    public static String getCurrentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            if (email != null && !email.trim().isEmpty()) {
                return email;
            }
        }
        HttpServletRequest request = getCurrentHttpRequest();
        if (request != null) {
            String headerEmail = request.getHeader("X-Authenticated-Email");
            if (headerEmail != null && !headerEmail.trim().isEmpty()) {
                return headerEmail.trim();
            }
        }
        return getCurrentUser();
    }

    public static String getCurrentFullName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt jwt) {
            String name = jwt.getClaimAsString("name");
            if (name != null && !name.trim().isEmpty()) {
                return name;
            }
            String given = jwt.getClaimAsString("given_name");
            String family = jwt.getClaimAsString("family_name");
            if (given != null || family != null) {
                return ((given != null ? given : "") + " " + (family != null ? family : "")).trim();
            }
        }
        return getCurrentUser();
    }

    public static Set<String> getCurrentRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            if (authorities != null && !authorities.isEmpty()) {
                return authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                        .map(String::toUpperCase)
                        .collect(Collectors.toSet());
            }
        }

        HttpServletRequest request = getCurrentHttpRequest();
        if (request != null) {
            String headerRole = request.getHeader("X-Authenticated-Role");
            if (headerRole != null && !headerRole.trim().isEmpty()) {
                String role = headerRole.trim().toUpperCase();
                if (role.startsWith("ROLE_")) {
                    role = role.substring(5);
                }
                return Set.of(role);
            }
        }

        return Collections.emptySet();
    }

    public static boolean hasRole(String role) {
        if (role == null) return false;
        String normalized = role.toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return getCurrentRoles().contains(normalized);
    }

    public static boolean isChecker() {
        return hasRole("CHECKER") || hasRole("ADMIN");
    }

    public static boolean isMaker() {
        return hasRole("MAKER");
    }

    public static boolean isCustomer() {
        return hasRole("CUSTOMER");
    }

    public static boolean isTpp() {
        return hasRole("TPP") || hasRole("CONSENT_REQUESTER");
    }

    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public static boolean isConsentManager() {
        return hasRole("CONSENT_MANAGER") || hasRole("ADMIN");
    }

    private static HttpServletRequest getCurrentHttpRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
