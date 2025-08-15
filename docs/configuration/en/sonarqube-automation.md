# SonarQube Automation with Quality Gate

## 🎯 Overview

This document describes the complete automation solution for SonarQube integration with the RecargaPay Wallet API, including automatic token generation, coverage validation, and quality gates that block deployment if coverage is below 90%.

## 🔧 Problem Solved

**Original Issue**: SonarQube forces password change on first login, breaking automation when Docker volumes are reset.

**Solution**: Complete automation that handles:
- ✅ Automatic credential management (handles password changes)
- ✅ Dynamic token generation via API
- ✅ Quality gate enforcement (90% coverage requirement)
- ✅ Resilience to volume resets and container restarts
- ✅ Detailed error reporting and troubleshooting

## 🚀 How It Works

### 1. **Docker Compose Configuration**
```yaml
sonarqube:
  image: sonarqube:10.4-community
  environment:
    - SONAR_FORCEAUTHENTICATION=false
    - SONAR_SECURITY_REALM=
    - SONAR_WEB_JAVAADDITIONALOPTS=-Dsonar.web.javaAdditionalOpts=-Dsonar.security.realm=
```

### 2. **Automated Credential Management**
The system automatically:
- Tries default credentials (`admin:admin`)
- If forced to change, uses `admin:admin123`
- Handles both scenarios transparently
- Works after volume resets

### 3. **Token Generation Process**
```bash
# Automatic token generation via API
curl -X POST -u "admin:admin" \
  -d "name=wallet-api-automation-token" \
  -d "type=USER_TOKEN" \
  "http://localhost:9000/api/user_tokens/generate"
```

### 4. **Quality Gate Enforcement**
```bash
# Maven command with coverage validation
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=recargapay-wallet-api \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<generated-token> \
  -s settings.xml
```

## 📋 Usage

### **Automated Startup (Recommended)**
```bash
./wallet-api-startup.sh
```

The script automatically:
1. ✅ Starts SonarQube container
2. ✅ Waits for SonarQube to be ready
3. ✅ Sets up credentials automatically
4. ✅ Generates authentication token
5. ✅ Runs Maven build with SonarQube analysis
6. ✅ Validates coverage >= 90%
7. ✅ **BLOCKS deployment if coverage < 90%**

### **Manual SonarQube Initialization**
```bash
./scripts/init-sonarqube.sh
```

## 🔍 Quality Gate Details

### **Coverage Requirement**
- **Minimum**: 90% instruction coverage
- **Enforcement**: Hard block (script exits with error code 1)
- **Validation**: Real-time via SonarQube API

### **When Coverage < 90%**
```
❌ Coverage requirement NOT MET! (85.2% < 90%)
🚫 Quality Gate FAILED - Deployment blocked

📈 To fix this issue:
   1. Add more unit tests to increase coverage
   2. Focus on untested classes and methods
   3. Run 'mvn clean test jacoco:report' to see detailed coverage report
   4. Check target/site/jacoco/index.html for coverage details
```

### **When Coverage >= 90%**
```
✅ Coverage requirement MET! (92.5% >= 90%)
🎉 Quality Gate PASSED - Proceeding with deployment
```

## 🛠️ Technical Implementation

### **Key Components**

1. **`wait_for_sonarqube_ready()`**
   - Waits up to 5 minutes for SonarQube startup
   - Checks `/api/system/status` endpoint
   - Ensures full operational readiness

2. **`setup_sonarqube_credentials()`**
   - Handles default credentials (`admin:admin`)
   - Manages password change requirement
   - Returns working credentials for token generation

3. **`get_sonarqube_token()`**
   - Generates new authentication token via API
   - Handles token conflicts (revokes existing if needed)
   - Returns valid token for Maven analysis

4. **`validate_coverage_from_api()`**
   - Retrieves coverage data via SonarQube API
   - Waits for analysis processing (up to 2 minutes)
   - Returns precise coverage percentage

### **Maven Integration**
```xml
<!-- pom.xml - SonarQube configuration -->
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.9.1.2184</version>
</plugin>
```

## 🔧 Configuration Files

### **Docker Compose**
- **File**: `docker-compose.yml`
- **Service**: `sonarqube`
- **Key Settings**: Disabled forced authentication

### **Maven Settings**
- **File**: `settings.xml`
- **Purpose**: Maven configuration for SonarQube integration

### **Initialization Script**
- **File**: `scripts/init-sonarqube.sh`
- **Purpose**: Manual SonarQube setup and quality gate configuration

## 🚨 Troubleshooting

### **Common Issues**

#### **SonarQube Not Starting**
```bash
# Check container status
docker-compose ps sonarqube

# Check logs
docker-compose logs sonarqube

# Restart if needed
docker-compose restart sonarqube
```

#### **Token Generation Failed**
```bash
# Check SonarQube API accessibility
curl -s "http://localhost:9000/api/system/status"

# Manual token generation
curl -X POST -u "admin:admin" \
  -d "name=test-token" \
  "http://localhost:9000/api/user_tokens/generate"
```

#### **Coverage Data Not Available**
```bash
# Check if analysis completed
curl -s -u "admin:admin" \
  "http://localhost:9000/api/measures/component?component=recargapay-wallet-api&metricKeys=coverage"

# Run analysis manually
mvn clean verify sonar:sonar -Dsonar.host.url=http://localhost:9000
```

### **Volume Reset Recovery**
When Docker volumes are reset:
1. ✅ Script automatically detects reset
2. ✅ Reconfigures credentials
3. ✅ Regenerates tokens
4. ✅ Continues normally

## 📊 Monitoring and Reporting

### **SonarQube Dashboard**
- **URL**: http://localhost:9000
- **Project**: `recargapay-wallet-api`
- **Credentials**: `admin:admin` or `admin:admin123`

### **Coverage Reports**
- **Jacoco HTML**: `target/site/jacoco/index.html`
- **SonarQube**: http://localhost:9000/dashboard?id=recargapay-wallet-api

### **Log Files**
- **Startup**: `logs/startup_YYYYMMDD_HHMMSS.log`
- **Maven**: `logs/maven_YYYYMMDD_HHMMSS.log`
- **SonarQube**: `logs/sonar_YYYYMMDD_HHMMSS.log`

## ✅ Success Criteria

The automation is successful when:
- ✅ SonarQube starts automatically
- ✅ Credentials are managed transparently
- ✅ Token generation works consistently
- ✅ Coverage validation is accurate
- ✅ Quality gate blocks deployment when coverage < 90%
- ✅ System recovers from volume resets
- ✅ Detailed error messages guide developers

## 🎉 Benefits

1. **Zero Manual Intervention**: Fully automated SonarQube integration
2. **Quality Enforcement**: Hard block on insufficient coverage
3. **Resilient**: Handles infrastructure resets gracefully
4. **Developer Friendly**: Clear error messages and guidance
5. **CI/CD Ready**: Perfect for automated pipelines
6. **Consistent**: Same behavior across all environments

---

## 🔗 Related Documentation

- [Team Onboarding](../../onboarding/en/team-onboarding.md)
- [Environment Setup](environment-setup.md)
- [Docker Versions](docker-versions.md)

---

*This automation ensures that only high-quality code with adequate test coverage reaches production, maintaining the reliability and maintainability of the RecargaPay Wallet API.*

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Automação SonarQube](../pt/sonarqube-automation.md)

---

*For more information, see the [main project documentation](../../../README.md).*
