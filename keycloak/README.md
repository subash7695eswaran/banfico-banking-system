# Keycloak Identity & Access Management (Week 4)

## Overview
Keycloak provides OAuth2 / OpenID Connect authentication, JWT issuance, and Role-Based Access Control (RBAC) for the Banfico Mini Banking application.

## Realm Details
- **Realm Name**: `banfico-realm`
- **Client ID**: `banfico-banking-app`
- **Token Endpoint**: `http://localhost:8081/realms/banfico-realm/protocol/openid-connect/token`
- **JWKS URL**: `http://localhost:8081/realms/banfico-realm/protocol/openid-connect/certs`

## Pre-configured Test Accounts
| Username | Password | Roles | Use Case |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `admin` | Full system access, create accounts, delete beneficiaries |
| `maker_user` | `maker123` | `maker` | Initiate transactions, create accounts |
| `checker_user` | `checker123` | `checker` | Approve/Reject transactions, review consents |
| `customer_user` | `customer123` | `customer` | View own accounts, transaction history, manage consents |

## Getting a JWT via cURL / Postman
```bash
curl -X POST http://localhost:8081/realms/banfico-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=banfico-banking-app" \
  -d "username=admin" \
  -d "password=admin123"
```
