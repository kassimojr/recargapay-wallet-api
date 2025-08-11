# 🔒 Security & Authentication

This section covers all security aspects of the RecargaPay Wallet API, including authentication, authorization, and security best practices.

## 📋 Quick Navigation

| 📄 Document | 📝 Description | 🎯 Audience |
|-------------|----------------|-------------|
| [Security Configuration](security-config.md) | Complete security setup and configuration | Developers, DevOps |
| [JWT Authentication](security-config.md) | JWT token configuration and usage | Developers |
| [Environment Security](../../configuration/en/environment-setup.md#security-configuration) | Environment-specific security settings | DevOps, SysAdmin |

## 🎯 Security Overview

### Authentication & Authorization
- **JWT-based authentication** with configurable secrets
- **Role-based access control** for different endpoints
- **Method-level security** annotations
- **Actuator endpoint protection** by environment

### Security Headers
- **CORS configuration** for cross-origin requests
- **Security headers** automatically configured
- **Content Security Policy** implementation
- **XSS and CSRF protection**

### Environment Security
- **No hardcoded credentials** in source code
- **Environment variable validation** at startup
- **Fail-fast security** - app won't start without proper config
- **Different security levels** per environment (dev/test/hml/prod)

## 🚀 Quick Start

### For Developers
1. **Setup JWT**: [JWT Configuration](security-config.md)
2. **Configure environment**: [Security Config](security-config.md)
3. **Test authentication**: Use provided API collections

### For DevOps
1. **Environment setup**: [Environment Security](../../configuration/en/environment-setup.md#security-configuration)
2. **Production hardening**: [Security Config](security-config.md)
3. **Monitoring setup**: [Monitoring Guide](../../monitoring/en/README.md)

## 🔗 Related Documentation

- **🏠 Main Documentation**: [Project README](../../../README.md)
- **⚙️ Configuration**: [Environment Setup](../../configuration/en/)
- **🚀 Onboarding**: [Team Setup](../../onboarding/en/)
- **📊 Monitoring**: [Monitoring Guide](../../monitoring/en/README.md)

## 🛡️ Security Best Practices

### Development
- Always use environment variables for sensitive data
- Never commit `.env` files or secrets
- Use strong JWT secrets (minimum 256 bits)
- Test security configurations regularly

### Production
- Use separate management ports for actuator
- Restrict actuator endpoints to minimum necessary
- Implement proper network security
- Monitor security events and access patterns

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [README em Português](../pt/README.md)

---

*For more information, see the [main project documentation](../../../README.md).*
