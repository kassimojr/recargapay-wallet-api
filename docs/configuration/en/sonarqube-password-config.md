# SonarQube Password Configuration Guide

## 🔐 Overview

This guide explains how to configure SonarQube passwords for different environments and scenarios, ensuring robust automation even when passwords change.

## 🎯 Supported Scenarios

### ✅ Password Change Scenarios Covered:
1. **Manual Password Changes** - Admin changes password via UI
2. **Corporate Security Policies** - Forced password rotation
3. **Environment Differences** - Different passwords per environment (dev/staging/prod)
4. **CI/CD Automation** - Secrets management integration
5. **First-time Setup** - Automated handling of forced password reset

## ⚙️ Configuration Methods

### 1. Environment Variables (Highest Priority)
```bash
export SONAR_USER="admin"
export SONAR_PASS="your_current_password"
export SONAR_NEW_PASS="your_new_password"

# Alternative naming convention
export SONARQUBE_USER="admin"
export SONARQUBE_PASSWORD="your_current_password"
export SONARQUBE_NEW_PASSWORD="your_new_password"
```

### 2. .env File Configuration
Create a `.env` file in your project root:
```bash
# SonarQube Authentication Configuration
SONAR_USER=admin
SONAR_PASS=your_current_password
SONAR_NEW_PASS=your_new_password

# Alternative naming (both supported)
SONARQUBE_USER=admin
SONARQUBE_PASSWORD=your_current_password
SONARQUBE_NEW_PASSWORD=your_new_password
```

### 3. Fallback Password Candidates
The script automatically tries these passwords in order:
1. Primary password from env/config
2. `admin` (default)
3. `admin123` (common changed password)
4. Configured new password
5. `sonar` (alternative default)
6. `password` (common fallback)

## 🚀 Usage Examples

### Development Environment
```bash
# .env file
SONAR_USER=admin
SONAR_PASS=admin
SONAR_NEW_PASS=dev123
```

### Staging Environment
```bash
# Environment variables
export SONAR_USER="admin"
export SONAR_PASS="staging_secure_password"
export SONAR_NEW_PASS="staging_new_password"
```

### Production Environment (CI/CD)
```bash
# Using secrets management
export SONAR_USER="${VAULT_SONAR_USER}"
export SONAR_PASS="${VAULT_SONAR_PASSWORD}"
export SONAR_NEW_PASS="${VAULT_SONAR_NEW_PASSWORD}"
```

## 🔧 Advanced Configuration

### Custom Password Candidates
You can modify the script to include your organization's common passwords:

```bash
# In setup-sonarqube-automation.sh
SONAR_PASSWORD_CANDIDATES=(
    "$SONAR_PASS"           # Primary from env/config
    "admin"                 # Default
    "admin123"              # Common changed password
    "$SONAR_NEW_PASS"       # Configured new password
    "your_org_default"      # Add your organization's defaults
    "your_common_password"  # Add common passwords
)
```

## 🛡️ Security Best Practices

### ✅ Recommended Practices:
- **Use environment variables** for production
- **Never commit passwords** to version control
- **Use secrets management** in CI/CD pipelines
- **Rotate passwords regularly**
- **Use strong passwords** (avoid defaults)

### ❌ Avoid:
- Hardcoding passwords in scripts
- Using default passwords in production
- Committing `.env` files with real passwords
- Sharing passwords in plain text

## 🔍 Troubleshooting

### Password Authentication Failed
1. **Check current password**: Verify the password is correct
2. **Try manual login**: Test credentials in SonarQube UI
3. **Check environment variables**: Ensure variables are set correctly
4. **Review logs**: Check script output for authentication attempts

### Script Can't Find Password
1. **Verify .env file**: Ensure file exists and has correct format
2. **Check variable names**: Use supported variable names
3. **Test environment variables**: `echo $SONAR_PASS`
4. **Review fallback candidates**: Add your passwords to candidates list

### Multiple Environment Issues
1. **Use different .env files**: `.env.dev`, `.env.staging`, `.env.prod`
2. **Environment-specific variables**: Use CI/CD environment variables
3. **Conditional configuration**: Script can load different configs per environment

## 📋 Configuration Checklist

- [ ] Choose configuration method (env vars or .env file)
- [ ] Set SONAR_USER (default: admin)
- [ ] Set SONAR_PASS (current password)
- [ ] Set SONAR_NEW_PASS (password to change to)
- [ ] Test authentication with `./scripts/setup-sonarqube-automation.sh`
- [ ] Verify automation works with `./wallet-api-startup.sh`
- [ ] Document passwords in your secrets management system

## 🔄 Password Rotation Workflow

### When Password Changes:
1. **Update configuration** (env vars or .env)
2. **Test authentication** with new password
3. **Update CI/CD secrets** if applicable
4. **Run automation script** to verify
5. **Update documentation** for team

### Automated Rotation:
```bash
# Example rotation script
OLD_PASS="$SONAR_PASS"
NEW_PASS="$(generate_secure_password)"

export SONAR_PASS="$OLD_PASS"
export SONAR_NEW_PASS="$NEW_PASS"

# Run automation
./scripts/setup-sonarqube-automation.sh

# Update secrets management
update_vault_secret "SONAR_PASS" "$NEW_PASS"
```

## 🎯 Integration Examples

### GitHub Actions
```yaml
env:
  SONAR_USER: ${{ secrets.SONAR_USER }}
  SONAR_PASS: ${{ secrets.SONAR_PASSWORD }}
  SONAR_NEW_PASS: ${{ secrets.SONAR_NEW_PASSWORD }}
```

### GitLab CI
```yaml
variables:
  SONAR_USER: $SONAR_USER
  SONAR_PASS: $SONAR_PASSWORD
  SONAR_NEW_PASS: $SONAR_NEW_PASSWORD
```

### Jenkins
```groovy
environment {
    SONAR_USER = credentials('sonar-user')
    SONAR_PASS = credentials('sonar-password')
    SONAR_NEW_PASS = credentials('sonar-new-password')
}
```

---

## 🔗 Related Documentation

- [SonarQube Automation](sonarqube-automation.md)
- [Environment Setup](environment-setup.md)
- [Docker Versions](docker-versions.md)

---

## 📞 Support

If you encounter issues with password configuration:
1. Check this documentation
2. Review script logs
3. Test manual authentication
4. Contact your DevOps team for secrets management help

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Configuração de Senhas SonarQube](../pt/sonarqube-password-config.md)

---

*For more information, see the [main project documentation](../../../README.md).*
