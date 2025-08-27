# 🔒 Security Configuration Guide

This document provides a comprehensive overview of the security configuration system implemented in the Digital Wallet API, following industry best practices for secure application deployment.

## 📋 Executive Summary

The Digital Wallet API has been successfully migrated from hardcoded credentials to a secure, externalized configuration system. This implementation ensures zero risk of credential exposure while maintaining operational excellence across all environments.

## 🛡️ Security Improvements Implemented

### 1. **Eliminated Hardcoded Credentials**
- ✅ **Before**: Credentials embedded in source code (`admin`, `admin`)
- ✅ **After**: All sensitive data externalized via environment variables
- **Impact**: Zero risk of credential exposure in version control

### 2. **Implemented Fail-Fast Configuration**
- ✅ **Before**: Application started with default values if config missing
- ✅ **After**: Application fails to start without proper configuration
- **Impact**: Immediate detection of configuration issues, prevents production incidents

### 3. **Adopted Generic Naming Convention**
- ✅ **Before**: Role-specific names (`adminUsername`, `adminPassword`)
- ✅ **After**: Generic names (`ADMIN_USERNAME`, `ADMIN_PASSWORD`)
- **Impact**: Reduced information disclosure, improved maintainability

### 4. **Enhanced Configuration Hierarchy**
```
Priority Order (Highest to Lowest):
1. System Environment Variables
2. .env file (development)
3. application-{profile}.yml
4. application.yml
```

## ⚙️ Current Configuration Structure

### Application Properties
```yaml
# application.yml - Production (No Fallbacks)
app:
  user:
    username: ${ADMIN_USERNAME}          # Fails if not provided
    password: ${ADMIN_PASSWORD}          # Fails if not provided

# application-dev.yml - Development (Safe Fallbacks)
app:
  user:
    username: ${ADMIN_USERNAME:testuser}     # Fallback for dev only
    password: ${ADMIN_PASSWORD:testpass}     # Fallback for dev only
```

### Environment Variables
```bash
# Required for all environments
ADMIN_USERNAME=your_admin_username
ADMIN_PASSWORD=your_secure_admin_password
JWT_SECRET=your_super_secure_jwt_secret_key_here_minimum_256_bits

# Database security
DB_USERNAME=your_db_username
DB_PASSWORD=your_secure_db_password

# Redis security
REDIS_PASSWORD=redis_secure_password_here
```

## 🔐 JWT Security Configuration

### JWT Secret Requirements
- **Minimum length**: 256 bits (32 characters)
- **Complexity**: Mix of letters, numbers, and special characters
- **Uniqueness**: Different secret per environment
- **Rotation**: Regular secret rotation recommended

### JWT Configuration
```yaml
# JWT settings are handled by Spring Security
# Secret is loaded from environment variable
JWT_SECRET=${JWT_SECRET}
```

### Token Validation
```java
// Automatic validation by Spring Security
// Custom validation logic in SecurityConfig
@EnableWebSecurity
public class SecurityConfig {
    // JWT configuration with environment-based secret
}
```

## 🌍 Environment-Specific Security

### Development Environment
```yaml
# application-dev.yml
management:
  endpoints:
    web:
      exposure:
        include: "*"  # All endpoints for debugging
  endpoint:
    health:
      show-details: always  # Full health details
```

### Test Environment
```yaml
# application-test.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics  # Limited endpoints
  endpoint:
    health:
      show-details: always  # Full details for testing
```

### Homologation Environment
```yaml
# application-hml.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics  # Moderate restriction
  endpoint:
    health:
      show-details: when-authorized  # Authorized access only
```

### Production Environment
```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus  # Minimal endpoints
  endpoint:
    health:
      show-details: never  # No details exposure
  server:
    port: 9090  # Separate management port
```

## 🛡️ Security Headers & CORS

### Automatic Security Headers
The application automatically configures:
- **X-Content-Type-Options**: `nosniff`
- **X-Frame-Options**: `DENY`
- **X-XSS-Protection**: `1; mode=block`
- **Strict-Transport-Security**: HTTPS enforcement
- **Content-Security-Policy**: XSS protection

### CORS Configuration
```java
@CrossOrigin(origins = {"http://localhost:3000", "https://your-frontend.com"})
// Configured per environment requirements
```

## 🔍 Security Monitoring

### Health Checks
```bash
# Check application security status
curl http://localhost:8080/actuator/health

# Check security headers
curl -I http://localhost:8080/api/v1/wallets
```

### Authentication Testing
```bash
# Test login endpoint
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"your_admin_username","password":"your_admin_password"}'

# Test protected endpoint
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/wallets
```

## 🚨 Security Validation Checklist

### Pre-Deployment Security Check
- [ ] **Environment Variables**: All required variables set
- [ ] **JWT Secret**: Minimum 32 characters, unique per environment
- [ ] **Database Credentials**: Strong passwords, unique per environment
- [ ] **Redis Password**: Secure password configured
- [ ] **No Hardcoded Values**: No secrets in source code
- [ ] **Actuator Endpoints**: Properly restricted per environment
- [ ] **Health Details**: Appropriate exposure level set

### Runtime Security Validation
- [ ] **Authentication**: Login endpoint working correctly
- [ ] **Authorization**: Protected endpoints require valid JWT
- [ ] **Health Checks**: Security components showing as healthy
- [ ] **Error Handling**: No sensitive information in error responses
- [ ] **Logging**: Security events properly logged (without sensitive data)

## 🔧 Troubleshooting Security Issues

### Common Issues

#### 1. Application Won't Start
```bash
# Check if all required environment variables are set
env | grep -E "(ADMIN_|JWT_|DB_|REDIS_)"

# Verify JWT secret length
echo -n "$JWT_SECRET" | wc -c  # Should be >= 32
```

#### 2. Authentication Failures
```bash
# Test credentials
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"'$ADMIN_USERNAME'","password":"'$ADMIN_PASSWORD'"}'

# Check JWT secret configuration
grep -r "JWT_SECRET" src/main/resources/
```

#### 3. Actuator Access Issues
```bash
# Check actuator configuration per environment
curl http://localhost:8080/actuator/health
curl http://localhost:9090/actuator/health  # Production management port
```

## 📚 Security Best Practices

### Development
1. **Never commit `.env` files** to version control
2. **Use different secrets** for each environment
3. **Test security configurations** regularly
4. **Monitor security logs** for suspicious activity

### Production
1. **Use separate management ports** for actuator endpoints
2. **Implement network-level security** (firewalls, VPNs)
3. **Regular credential rotation** schedule
4. **Security monitoring and alerting** setup

### Team Guidelines
1. **Security training** for all team members
2. **Code review focus** on security configurations
3. **Incident response procedures** for security issues
4. **Regular security assessments** and updates

## 🔗 Related Documentation

- **🏠 Main Documentation**: [Project README](../../../README.md)
- **⚙️ Environment Setup**: [Configuration Guide](../../configuration/en/environment-setup.md)
- **🚀 Team Onboarding**: [Security Setup](../../onboarding/en/team-onboarding.md#step-2-verify-security)
- **📊 Security Monitoring**: [Monitoring Guide](../../monitoring/en/)

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Configuração de Segurança em Português](../pt/configuracao-seguranca.md)

---

*For more information, see the [main project documentation](../../../README.md).*
