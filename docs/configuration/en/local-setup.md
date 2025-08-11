# 🚀 Local Development Setup

This guide helps you set up the RecargaPay Wallet API for local development with proper environment configuration and security practices.

## 📋 Overview

The project uses environment variables for configuration management, eliminating hardcoded credentials and making local development more secure and flexible.

## ⚡ Quick Setup

### 1. Environment Configuration

Copy the environment template and configure your local values:

```bash
cp .env.template .env
```

Edit the `.env` file with your local configuration:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=recargapay_wallet
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password

# JWT Configuration (minimum 32 characters)
JWT_SECRET=your_jwt_secret_key_at_least_32_characters_long

# Admin User Configuration
ADMIN_USERNAME=your_admin_username
ADMIN_PASSWORD=your_admin_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# Cache Configuration
APP_CACHE_VERSION=v1
CACHE_TTL_DEFAULT_MINUTES=2
CACHE_TTL_WALLET_LIST_MINUTES=3
CACHE_TTL_WALLET_SINGLE_MINUTES=1
CACHE_TTL_WALLET_BALANCE_SECONDS=30
CACHE_TTL_WALLET_TRANSACTIONS_MINUTES=10
CACHE_TTL_USER_PROFILE_MINUTES=15

# Application Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Logging Configuration
LOGGING_LEVEL_ROOT=DEBUG
LOGGING_LEVEL_APP=DEBUG
```

### 2. Start Services

Start all required services using Docker Compose:

```bash
docker-compose up -d
```

This will start:
- **PostgreSQL** database on port 5432
- **Redis** cache on port 6379
- **Grafana** monitoring on port 3000
- **Loki** logging aggregation
- **Promtail** log collection

### 3. Start the Application

```bash
./mvnw spring-boot:run
```

The application will automatically load environment variables from the `.env` file.

### 4. Verify Setup

Check if everything is working:

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Test authentication
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"your_admin_username","password":"your_admin_password"}'
```

## 🔧 Alternative Configuration Methods

### Option 1: System Environment Variables

Set environment variables directly in your system:

```bash
export DB_USERNAME=your_database_username
export DB_PASSWORD=your_database_password
export JWT_SECRET=your_jwt_secret_key_at_least_32_characters_long
export ADMIN_USERNAME=your_admin_username
export ADMIN_PASSWORD=your_admin_password
```

### Option 2: IDE Configuration

Configure environment variables in your IDE:

#### IntelliJ IDEA
1. Go to **Run/Debug Configurations**
2. Select your Spring Boot configuration
3. Add environment variables in **Environment Variables** section

#### VS Code
1. Create `.vscode/launch.json`
2. Add environment variables in the configuration:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Spring Boot-WalletApiApplication",
      "request": "launch",
      "mainClass": "com.recargapay.wallet.WalletApiApplication",
      "env": {
        "DB_USERNAME": "your_database_username",
        "DB_PASSWORD": "your_database_password",
        "JWT_SECRET": "your_jwt_secret_key_at_least_32_characters_long",
        "ADMIN_USERNAME": "your_admin_username",
        "ADMIN_PASSWORD": "your_admin_password"
      }
    }
  ]
}
```

## 🐳 Docker Development

### Full Docker Setup

Run the entire application stack with Docker:

```bash
# Build the application
./mvnw clean package -DskipTests

# Start all services including the application
docker-compose -f docker-compose.yml -f docker-compose.override.yml up -d
```

### Database Only

If you prefer to run only the database in Docker:

```bash
docker-compose up -d postgres redis
```

Then run the application locally:

```bash
./mvnw spring-boot:run
```

## 🔍 Troubleshooting

### Common Issues

#### 1. Application Won't Start

**Problem**: Missing environment variables
```bash
# Check if all required variables are set
env | grep -E "(DB_|JWT_|ADMIN_|REDIS_|CACHE_)"
```

**Solution**: Ensure all required variables are set in `.env` or system environment

#### 2. Database Connection Issues

**Problem**: Cannot connect to PostgreSQL
```bash
# Test database connection
psql -h localhost -p 5432 -U your_username -d recargapay_wallet
```

**Solutions**:
- Ensure PostgreSQL is running: `docker-compose up -d postgres`
- Check database credentials in `.env`
- Verify database name and port

#### 3. Redis Connection Issues

**Problem**: Cannot connect to Redis
```bash
# Test Redis connection
redis-cli -h localhost -p 6379 -a your_redis_password ping
```

**Solutions**:
- Ensure Redis is running: `docker-compose up -d redis`
- Check Redis password in `.env`
- Verify Redis host and port

#### 4. Authentication Issues

**Problem**: Login fails
```bash
# Verify JWT secret length
echo -n "$JWT_SECRET" | wc -c  # Should be >= 32
```

**Solutions**:
- Ensure JWT secret is at least 32 characters
- Check admin credentials in `.env`
- Verify authentication endpoint is accessible

## 📊 Development Tools

### API Testing

Import the provided collections:
- **Postman**: [Collection](../../api/postman/)
- **Insomnia**: [Collection](../../api/insomnia/)

### Database Access

Connect to PostgreSQL:
```bash
# Command line
psql -h localhost -p 5432 -U your_username -d recargapay_wallet

# Or use a GUI tool like pgAdmin, DBeaver, etc.
```

### Cache Monitoring

Monitor Redis cache:
```bash
# Redis CLI
redis-cli -h localhost -p 6379 -a your_redis_password

# Monitor cache operations
redis-cli -h localhost -p 6379 -a your_redis_password MONITOR
```

### Observability

Access monitoring tools:
- **Grafana**: http://localhost:3000 (admin/admin)
- **Application Logs**: Check console output or `logs/wallet-api.json`

## 🔒 Security Notes

### Environment File Security

- ✅ **Never commit `.env` files** to version control
- ✅ **Use strong passwords** (minimum 12 characters)
- ✅ **Use unique JWT secrets** (minimum 32 characters)
- ✅ **Rotate credentials regularly**

### Development vs Production

- **Development**: Uses `.env` file for convenience
- **Production**: Uses system environment variables
- **Different secrets**: Each environment should have unique credentials

## 📚 Next Steps

After successful local setup:

1. **Explore the API**: [API Documentation](../../../README.md#api-reference)
2. **Understand Architecture**: [Architecture Guide](../../../README.md#architecture)
3. **Run Tests**: `./mvnw test`
4. **Check Code Coverage**: `./mvnw jacoco:report`
5. **Team Onboarding**: [Complete Checklist](../../onboarding/en/team-onboarding.md)

## 🔗 Related Documentation

- **🏠 Main Documentation**: [Project README](../../../README.md)
- **⚙️ Environment Setup**: [Complete Configuration Guide](environment-setup.md)
- **🔒 Security**: [Security Configuration](../../security/en/security-config.md)
- **🚀 Team Onboarding**: [Onboarding Guide](../../onboarding/en/)

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Configuração Local em Português](../pt/configuracao-local.md)

---

*For more information, see the [main project documentation](../../../README.md).*
