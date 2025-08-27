#!/bin/bash

# =============================================================================
# Digital Wallet API - Environment Generator
# =============================================================================
# This script generates a .env file with working values based on src/main/resources/templates/.env.template
# Purpose: Ensure 100% automated setup for testing and development
# =============================================================================

# Environment Generation Script
# This script generates the .env file with all necessary environment variables

# Load common utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/utils/common.sh"

# Ensure we're in the project root
ensure_project_root

print_step "Environment Generation"

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$PROJECT_ROOT/.env"
ENV_TEMPLATE="$PROJECT_ROOT/src/main/resources/templates/.env.template"

# Function to generate secure random string
generate_secure_key() {
    local length=${1:-32}
    openssl rand -base64 $length | tr -d "=+/" | cut -c1-$length
}

# Main function to generate .env
generate_env_file() {
    print_info "Generating .env file for Digital Wallet API..."
    
    # Check if template exists
    if [ ! -f "$ENV_TEMPLATE" ]; then
        print_error "src/main/resources/templates/.env.template not found at $ENV_TEMPLATE"
        exit 1
    fi
    
    # Backup existing .env if it exists
    if [ -f "$ENV_FILE" ]; then
        print_warning "Existing .env found, creating backup..."
        cp "$ENV_FILE" "$ENV_FILE.backup.$(date +%Y%m%d_%H%M%S)"
        print_success "Backup created: $ENV_FILE.backup.$(date +%Y%m%d_%H%M%S)"
    fi
    
    print_info "Creating .env based on template with working values..."
    
    # Generate .env with exact values specified by user
    cat > "$ENV_FILE" << 'EOF'
# Digital Wallet API - Local Environment (Auto-generated)
# Generated automatically for development and testing

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=walletdb
DB_USERNAME=admin
DB_PASSWORD=admin

# JWT Configuration
JWT_SECRET=my-super-secure-jwt-secret-key-for-development-at-least-32-characters-long

# Redis Configuration for Distributed Caching
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Cache Configuration
APP_CACHE_VERSION=v1

# Cache TTL Configuration (Financial Industry Standards)
CACHE_TTL_DEFAULT_MINUTES=2
CACHE_TTL_WALLET_LIST_MINUTES=3
CACHE_TTL_WALLET_SINGLE_MINUTES=1
CACHE_TTL_WALLET_BALANCE_SECONDS=30
CACHE_TTL_WALLET_TRANSACTIONS_MINUTES=10
CACHE_TTL_USER_PROFILE_MINUTES=15

# Application User Configuration
USER_NAME=admin
USER_PASSWORD=admin

# Application Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Logging Configuration
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_APP=DEBUG

# CORS Configuration for Cross-Origin Requests
# Comma-separated list of allowed origins for frontend applications
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001,http://localhost:8080
# Comma-separated list of allowed HTTP methods
APP_CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
# Allowed headers (use * for all or specify specific headers)
APP_CORS_ALLOWED_HEADERS=*
# Allow credentials (cookies, authorization headers) in CORS requests
APP_CORS_ALLOW_CREDENTIALS=true
# Maximum age for preflight requests cache (in seconds)
APP_CORS_MAX_AGE=3600

# SonarQube Configuration (default credentials for local development)
SONAR_USER=admin
SONAR_PASS=admin
SONAR_NEW_PASS=admin123

EOF
    
    # Set appropriate permissions
    chmod 600 "$ENV_FILE"
    
    print_success ".env file generated successfully!"
    print_info "Location: $ENV_FILE"
    print_info "Permissions: 600 (owner read/write only)"
    
    # Validate generated file
    if [ -f "$ENV_FILE" ] && [ -s "$ENV_FILE" ]; then
        local line_count=$(wc -l < "$ENV_FILE")
        print_success "Validation passed: $line_count lines generated"
        
        # Show key configurations
        print_info "Key configurations:"
        echo -e "  ${GREEN}Database:${NC} admin:admin@localhost:5432/walletdb"
        echo -e "  ${GREEN}Redis:${NC} localhost:6379 (no password)"
        echo -e "  ${GREEN}Server:${NC} localhost:8080"
        echo -e "  ${GREEN}Profile:${NC} dev"
        echo -e "  ${GREEN}Admin User:${NC} admin:admin"
        echo -e "  ${GREEN}JWT Secret:${NC} [32+ characters configured]"
        echo -e "  ${GREEN}CORS:${NC} localhost:3000,3001,8080"
        
    else
        print_error "Failed to generate .env file"
        exit 1
    fi
}

# Function to verify environment compatibility
verify_environment() {
    print_info "Verifying environment compatibility..."
    
    # Check required tools
    local missing_tools=()
    
    if ! command -v openssl &> /dev/null; then
        missing_tools+=("openssl")
    fi
    
    if [ ${#missing_tools[@]} -gt 0 ]; then
        print_warning "Missing tools: ${missing_tools[*]}"
        print_info "Continuing with fallback methods..."
    fi
    
    print_success "Environment verification completed"
}

# Main execution
main() {
    echo -e "\n${BLUE}🔧 Digital Wallet API - Environment Generator${NC}"
    echo -e "${BLUE}=================================================${NC}\n"
    
    verify_environment
    generate_env_file
    
    echo -e "\n${GREEN}🎉 Environment setup completed successfully!${NC}"
    echo -e "${BLUE}You can now run the main startup script:${NC}"
    echo -e "${YELLOW}./wallet-api-startup.sh${NC}\n"
}

# Run main function if script is executed directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
