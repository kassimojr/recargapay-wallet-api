# 🚀 Team Onboarding Checklist

Welcome to the Digital Wallet API team! Use this comprehensive checklist to ensure you're properly set up with all the tools, configurations, and knowledge needed to contribute effectively.

## 📋 Quick Start Checklist

### ✅ Pre-requisites
- [ ] Git repository cloned locally
- [ ] Java 21+ installed
- [ ] Maven 3.6+ installed
- [ ] Docker and Docker Compose installed (for database and Redis)
- [ ] Your favorite IDE configured (IntelliJ IDEA, VS Code, etc.)

### ✅ Environment Setup

#### Step 1: Create Local Configuration
- [ ] Copy environment template: `cp src/main/resources/templates/.env.template .env`
- [ ] Edit `.env` file with your personal values:
  ```bash
  # Database Configuration
  DB_USERNAME=your_database_username
  DB_PASSWORD=your_database_password
  
  # Security Configuration
  JWT_SECRET=your_jwt_secret_key_at_least_32_characters_long
  ADMIN_USERNAME=your_admin_username
  ADMIN_PASSWORD=your_admin_password
  
  # Redis Configuration
  REDIS_PASSWORD=your_redis_password
  ```

#### Step 2: Verify Security
- [ ] Confirm `.env` is in `.gitignore` (should not be committed)
- [ ] Use strong passwords (minimum 12 characters)
- [ ] Use unique JWT secret (minimum 32 characters)
- [ ] Never share your `.env` file with others

#### Step 3: Start Services
- [ ] Start all services: `docker-compose up -d`
- [ ] Verify services are running:
  ```bash
  docker ps  # Should show postgres, redis, loki, grafana, promtail
  ```
- [ ] Check service health:
  ```bash
  curl http://localhost:8080/actuator/health
  ```

#### Step 4: Test Application
- [ ] Compile application: `./mvnw clean compile`
- [ ] Run tests: `./mvnw test`
- [ ] Start application: `./mvnw spring-boot:run`
- [ ] Verify startup logs show no configuration errors
- [ ] Test authentication endpoint:
  ```bash
  curl -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username": "your_admin_username", "password": "your_admin_password"}'
  ```
- [ ] Confirm JWT token is returned

### ✅ Understanding the Architecture

#### Core Concepts
- [ ] **Hexagonal Architecture**: Understand ports and adapters pattern
- [ ] **Domain-Driven Design**: Familiarize with domain, application, and infrastructure layers
- [ ] **Cache Strategy**: Learn about Redis distributed caching implementation
- [ ] **Security Model**: Understand JWT authentication and authorization
- [ ] **Observability**: Get familiar with structured logging and tracing

#### Key Components
- [ ] **Domain Layer**: Business logic and entities (`com.digital.wallet.domain`)
- [ ] **Application Layer**: Use cases and services (`com.digital.wallet.application`)
- [ ] **Infrastructure Layer**: External integrations (`com.digital.wallet.infra`)
- [ ] **API Layer**: REST controllers and DTOs (`com.digital.wallet.api`)

### ✅ Development Workflow

#### Code Quality
- [ ] Run code coverage: `./mvnw jacoco:report`
- [ ] Check coverage report: `target/site/jacoco/index.html`
- [ ] Ensure tests pass: `./mvnw test`
- [ ] Follow coding standards and patterns used in the project

#### Testing Strategy
- [ ] **Unit Tests**: Test individual components in isolation
- [ ] **Integration Tests**: Test component interactions
- [ ] **API Tests**: Use Postman/Insomnia collections provided
- [ ] **Cache Tests**: Verify Redis cache behavior

#### Git Workflow
- [ ] Create feature branches from `main`
- [ ] Use conventional commit messages
- [ ] Create pull requests for code review
- [ ] Ensure CI/CD pipeline passes

### ✅ Tools and Resources

#### Development Tools
- [ ] **API Testing**: Import [Postman Collection](../../api/postman/) or [Insomnia Collection](../../api/insomnia/)
- [ ] **Database**: Access PostgreSQL via `localhost:5432`
- [ ] **Cache**: Access Redis via `localhost:6379`
- [ ] **Monitoring**: Access Grafana at `http://localhost:3000`

#### Documentation Access
- [ ] **Main Documentation**: [Project README](../../../README.md)
- [ ] **Configuration Guide**: [Environment Setup](../../configuration/en/environment-setup.md)
- [ ] **Cache Documentation**: [Redis Setup](../../caching/en/redis-cache-setup.md)
- [ ] **Monitoring Guide**: [Observability Setup](../../monitoring/en/)
- [ ] **Tracing Guide**: [Distributed Tracing](../../tracing/en/)

### ✅ Team Integration

#### Communication
- [ ] Join team communication channels
- [ ] Introduce yourself to the team
- [ ] Schedule 1:1 with team lead/mentor
- [ ] Understand team processes and ceremonies

#### Knowledge Transfer
- [ ] Review recent pull requests to understand code patterns
- [ ] Attend team meetings and technical discussions
- [ ] Ask questions - the team is here to help!
- [ ] Document any improvements or suggestions you have

### ✅ First Contributions

#### Getting Started Tasks
- [ ] Fix a small bug or typo
- [ ] Add a unit test for existing functionality
- [ ] Improve documentation or comments
- [ ] Optimize a query or cache usage

#### Escalation Path
1. **Technical Questions**: Ask team members or tech lead
2. **Environment Issues**: Check [troubleshooting guide](../../configuration/en/environment-setup.md#troubleshooting)
3. **Architecture Questions**: Review documentation or schedule architecture session
4. **Urgent Issues**: Contact team lead immediately

### 🎯 Success Criteria

You're ready to contribute when you can:
- [ ] Start the application locally without errors
- [ ] Run the full test suite successfully
- [ ] Make API calls using authentication
- [ ] Understand the basic architecture and data flow
- [ ] Access monitoring and logging tools
- [ ] Create a simple feature or fix following team patterns

### 📞 Emergency Contacts

- **Security Issues**: Contact security team immediately
- **Production Issues**: Follow incident response procedures
- **General Questions**: Team chat or email

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Integração do Time em Português](../pt/integracao-time.md)

---

*For more information, see the [main project documentation](../../../README.md).*
