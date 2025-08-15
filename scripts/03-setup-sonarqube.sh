#!/bin/bash

# =============================================================================
# SonarQube Automation Setup Script
# =============================================================================
# This script handles the initial SonarQube setup to avoid manual password reset
# and ensures full automation compatibility for CI/CD pipelines
# =============================================================================

# SonarQube Setup and Automation Script
# This script handles SonarQube password reset, configuration, and token generation

# Load common utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/utils/common.sh"

# Ensure we're in the project root
ensure_project_root

print_step "SonarQube Setup and Automation"

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

# Configuration - Flexible password management
SONAR_HOST_URL="http://localhost:9000"

# Password configuration with multiple fallbacks
# Priority: 1) Environment variables, 2) .env file, 3) Default values
SONAR_USER="${SONAR_USER:-${SONARQUBE_USER:-admin}}"
SONAR_PASS="${SONAR_PASS:-${SONARQUBE_PASSWORD:-admin}}"
SONAR_NEW_PASS="${SONAR_NEW_PASS:-${SONARQUBE_NEW_PASSWORD:-admin123}}"

# Additional password candidates for fallback attempts
SONAR_PASSWORD_CANDIDATES=(
    "$SONAR_PASS"           # Primary from env/config
    "admin"                 # Default
    "admin123"              # Common changed password
    "$SONAR_NEW_PASS"       # Configured new password
    "sonar"                 # Alternative default
    "password"              # Common fallback
)

# Function to load configuration from .env file if available
load_env_config() {
    if [ -f ".env" ]; then
        print_info "Loading SonarQube configuration from .env file..."
        
        # Extract SonarQube related variables from .env
        if grep -q "SONAR_USER\|SONARQUBE_USER" .env; then
            SONAR_USER=$(grep -E "^(SONAR_USER|SONARQUBE_USER)=" .env | cut -d'=' -f2 | tr -d '"' | head -1)
        fi
        
        if grep -q "SONAR_PASS\|SONARQUBE_PASSWORD" .env; then
            SONAR_PASS=$(grep -E "^(SONAR_PASS|SONARQUBE_PASSWORD)=" .env | cut -d'=' -f2 | tr -d '"' | head -1)
        fi
        
        if grep -q "SONAR_NEW_PASS\|SONARQUBE_NEW_PASSWORD" .env; then
            SONAR_NEW_PASS=$(grep -E "^(SONAR_NEW_PASS|SONARQUBE_NEW_PASSWORD)=" .env | cut -d'=' -f2 | tr -d '"' | head -1)
        fi
        
        print_success "Configuration loaded from .env"
    fi
}

# Function to try authentication with multiple password candidates
try_authentication() {
    local endpoint="$1"
    local method="${2:-GET}"
    local additional_params="$3"
    
    print_info "Trying authentication for endpoint: $endpoint"
    
    # Try with current configured password first
    local auth_response=$(curl -s -w "%{http_code}" -X "$method" \
        -u "$SONAR_USER:$SONAR_PASS" \
        $additional_params \
        "$SONAR_HOST_URL$endpoint")
    
    local http_code="${auth_response: -3}"
    local response_body="${auth_response%???}"
    
    if [ "$http_code" = "200" ] || [ "$http_code" = "204" ]; then
        print_success "Authentication successful with current password"
        echo "$auth_response"
        return 0
    fi
    
    # Try with all password candidates
    for password in "${SONAR_PASSWORD_CANDIDATES[@]}"; do
        if [ "$password" != "$SONAR_PASS" ]; then  # Skip already tried password
            print_info "Trying authentication with alternative password..."
            
            auth_response=$(curl -s -w "%{http_code}" -X "$method" \
                -u "$SONAR_USER:$password" \
                $additional_params \
                "$SONAR_HOST_URL$endpoint")
            
            http_code="${auth_response: -3}"
            response_body="${auth_response%???}"
            
            if [ "$http_code" = "200" ] || [ "$http_code" = "204" ]; then
                print_success "Authentication successful with alternative password"
                SONAR_PASS="$password"  # Update current password
                echo "$auth_response"
                return 0
            fi
        fi
    done
    
    print_error "All authentication attempts failed"
    echo "$auth_response"
    return 1
}

# Function to wait for SonarQube to be ready
wait_for_sonarqube() {
    local max_attempts=30
    local attempt=1
    
    print_info "Waiting for SonarQube to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        local response=$(try_authentication "/api/system/status")
        local http_code="${response: -3}"
        
        if [ "$http_code" = "200" ]; then
            print_success "SonarQube is ready!"
            return 0
        fi
        
        echo -n "."
        sleep 5
        ((attempt++))
    done
    
    print_error "SonarQube failed to become ready"
    return 1
}

# Function to reset password via database (fallback method)
reset_password_via_database() {
    print_info "Attempting password reset via database..."
    
    # Database connection details from docker-compose
    local db_host="localhost"
    local db_port="5433"  # From docker-compose.yml
    local db_name="sonarqube"
    local db_user="sonarqube"
    local db_pass="sonarqube"
    
    # SQL command to reset admin password to 'admin' and force password change
    local reset_sql="UPDATE users SET 
        crypted_password='100000\$t2h8AtNs1AlCHuLobDjHQTn9XppwTIx88UjqUm4s8RsfTuXQHSd/fpFexAnewwPsO6jGFQUv/24DnO55hY6Xew==', 
        salt='k9x9eN127/3e/hf38iNiKwVfaVk=', 
        hash_method='PBKDF2', 
        reset_password=true, 
        user_local=true 
        WHERE login='admin';"
    
    # Try to execute the SQL command
    if command -v psql >/dev/null 2>&1; then
        print_info "Using psql to reset password..."
        PGPASSWORD="$db_pass" psql -h "$db_host" -p "$db_port" -U "$db_user" -d "$db_name" -c "$reset_sql" 2>/dev/null
        if [ $? -eq 0 ]; then
            print_success "Password reset via database successful"
            SONAR_PASS="admin"  # Reset to default after database reset
            return 0
        fi
    fi
    
    # Try using docker exec if psql is not available
    print_info "Trying database reset via docker exec..."
    docker exec postgres-sonar psql -U "$db_user" -d "$db_name" -c "$reset_sql" 2>/dev/null
    if [ $? -eq 0 ]; then
        print_success "Password reset via docker exec successful"
        SONAR_PASS="admin"  # Reset to default after database reset
        return 0
    fi
    
    print_warning "Database password reset failed"
    return 1
}

# Enhanced function to handle forced password reset
handle_forced_password_reset() {
    print_info "Handling forced password reset scenario..."
    
    # Step 1: Try API-based password change first
    print_info "Attempting API-based password change..."
    
    # Try with current credentials to change password
    local change_response=$(curl -s -w "%{http_code}" -X POST \
        -u "$SONAR_USER:$SONAR_PASS" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "login=$SONAR_USER&password=$SONAR_NEW_PASS&previousPassword=$SONAR_PASS" \
        "$SONAR_HOST_URL/api/users/change_password")
    
    local change_code="${change_response: -3}"
    
    if [ "$change_code" = "204" ] || [ "$change_code" = "200" ]; then
        print_success "API-based password change successful"
        SONAR_PASS="$SONAR_NEW_PASS"  # Update current password
        return 0
    fi
    
    # Step 2: Try form-based reset for forced password change
    print_info "Attempting form-based password reset..."
    
    # Get session cookies and CSRF tokens
    local session_response=$(curl -s -c /tmp/sonar_cookies.txt -b /tmp/sonar_cookies.txt \
        -u "$SONAR_USER:$SONAR_PASS" \
        "$SONAR_HOST_URL/account/reset_password")
    
    # Extract CSRF token if present
    local csrf_token=$(echo "$session_response" | grep -o 'name="csrf-token"[^>]*content="[^"]*"' | sed 's/.*content="\([^"]*\)".*/\1/')
    
    # Submit password reset form
    local form_data="oldPassword=$SONAR_PASS&password=$SONAR_NEW_PASS&passwordConfirmation=$SONAR_NEW_PASS"
    if [ -n "$csrf_token" ]; then
        form_data="$form_data&csrf-token=$csrf_token"
    fi
    
    local form_response=$(curl -s -w "%{http_code}" -X POST \
        -b /tmp/sonar_cookies.txt \
        -u "$SONAR_USER:$SONAR_PASS" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -H "X-Requested-With: XMLHttpRequest" \
        -d "$form_data" \
        "$SONAR_HOST_URL/account/reset_password")
    
    local form_code="${form_response: -3}"
    
    if [ "$form_code" = "200" ] || [ "$form_code" = "302" ]; then
        print_success "Form-based password reset successful"
        SONAR_PASS="$SONAR_NEW_PASS"  # Update current password
        return 0
    fi
    
    # Step 3: Database reset as fallback
    print_info "API methods failed, trying database reset..."
    if reset_password_via_database; then
        # After database reset, try to change to desired password
        sleep 2  # Wait for changes to take effect
        
        local post_reset_response=$(curl -s -w "%{http_code}" -X POST \
            -u "$SONAR_USER:admin" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "login=$SONAR_USER&password=$SONAR_NEW_PASS&previousPassword=admin" \
            "$SONAR_HOST_URL/api/users/change_password")
        
        local post_reset_code="${post_reset_response: -3}"
        
        if [ "$post_reset_code" = "204" ] || [ "$post_reset_code" = "200" ]; then
            print_success "Password changed to desired value after database reset"
            SONAR_PASS="$SONAR_NEW_PASS"
        else
            print_info "Using default password 'admin' after database reset"
            SONAR_PASS="admin"
        fi
        return 0
    fi
    
    # Step 4: Try alternative authentication methods
    print_info "Trying alternative authentication approaches..."
    
    # Test if any of our password candidates work
    for password in "${SONAR_PASSWORD_CANDIDATES[@]}"; do
        local test_response=$(curl -s -w "%{http_code}" -u "$SONAR_USER:$password" \
            "$SONAR_HOST_URL/api/authentication/validate")
        
        local test_code="${test_response: -3}"
        local test_body="${test_response%???}"
        
        if [ "$test_code" = "200" ] && echo "$test_body" | grep -q '"valid":true'; then
            print_success "Found working password: $password"
            SONAR_PASS="$password"
            return 0
        fi
    done
    
    print_warning "All password reset methods failed, manual intervention may be required"
    return 1
}

# Function to disable security features for automation
configure_automation_settings() {
    print_info "Configuring SonarQube for automation-friendly operation..."
    
    # Try to disable force authentication via API (if available)
    local settings_response=$(try_authentication "/api/settings/set" "POST" "-d key=sonar.forceAuthentication&value=false")
    local settings_code="${settings_response: -3}"
    
    if [ "$settings_code" = "204" ] || [ "$settings_code" = "200" ]; then
        print_success "Force authentication disabled via API"
    else
        print_info "Could not disable force authentication via API (may not be available)"
    fi
}

# Function to create automation token
create_automation_token() {
    print_info "Creating automation token for CI/CD..."
    
    local token_name="automation-token-$(date +%s)"
    
    # Try to create token
    local token_response=$(try_authentication "/api/user_tokens/generate" "POST" "-d name=$token_name")
    local token=$(echo "$token_response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    
    if [ -n "$token" ] && [ "$token" != "null" ]; then
        print_success "Automation token created successfully"
        print_info "Token: ${token:0:20}..."
        echo "$token" > /tmp/sonar_automation_token.txt
        return 0
    fi
    
    print_warning "Could not create automation token, but setup can continue"
    return 0
}

# Function to check current authentication status
check_authentication() {
    print_info "Checking current authentication status..."
    
    # Try to validate current authentication with all password candidates
    local auth_response=$(try_authentication "/api/authentication/validate")
    local http_code="${auth_response: -3}"
    local response_body="${auth_response%???}"
    
    if [ "$http_code" = "200" ] && echo "$response_body" | grep -q '"valid":true'; then
        print_success "Authentication successful - no password reset needed"
        return 0
    fi
    
    print_info "Authentication failed - password reset may be required"
    return 1
}

# Main setup function
main() {
    echo -e "\n${BLUE}🔧 SonarQube Automation Setup${NC}"
    echo -e "${BLUE}==============================${NC}\n"
    
    # Load configuration from .env file if available
    load_env_config
    
    # Wait for SonarQube to be ready
    if ! wait_for_sonarqube; then
        print_error "SonarQube is not ready, cannot proceed with setup"
        exit 1
    fi
    
    # Check authentication status
    if check_authentication; then
        print_info "No password reset needed, proceeding with setup..."
    else
        # Handle password reset
        handle_forced_password_reset
    fi
    
    # Configure automation settings
    configure_automation_settings
    
    # Create automation token
    create_automation_token
    
    echo -e "\n${GREEN}🎉 SonarQube automation setup completed!${NC}"
    echo -e "${BLUE}SonarQube is now configured for automated CI/CD pipelines${NC}\n"
}

# Run main function if script is executed directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
