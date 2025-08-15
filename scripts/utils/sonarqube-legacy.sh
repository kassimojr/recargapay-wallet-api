#!/bin/bash

# =============================================================================
# SonarQube Initialization Script
# =============================================================================
# This script ensures SonarQube is properly configured for automation:
# 1. Waits for SonarQube to be ready
# 2. Handles password change requirements
# 3. Creates quality gates with 90% coverage requirement
# 4. Sets up project-specific configurations
# =============================================================================

set -e

# Configuration
SONAR_HOST_URL="http://localhost:9000"
PROJECT_KEY="recargapay-wallet-api"
REQUIRED_COVERAGE=90

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
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

# Wait for SonarQube to be ready
wait_for_sonarqube() {
    local max_attempts=60
    local attempt=1
    
    print_info "Waiting for SonarQube to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$SONAR_HOST_URL/api/system/status" | grep -q '"status":"UP"'; then
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

# Setup credentials and return working credentials
setup_credentials() {
    local default_user="admin"
    local default_pass="admin"
    local new_pass="admin123"
    
    print_info "Setting up SonarQube credentials..."
    
    # Try default credentials first
    if curl -s -u "$default_user:$default_pass" "$SONAR_HOST_URL/api/authentication/validate" | grep -q '"valid":true'; then
        print_success "Default credentials are working"
        echo "$default_user:$default_pass"
        return 0
    fi
    
    # Try with changed password
    if curl -s -u "$default_user:$new_pass" "$SONAR_HOST_URL/api/authentication/validate" | grep -q '"valid":true'; then
        print_success "Using previously changed credentials"
        echo "$default_user:$new_pass"
        return 0
    fi
    
    # Try to change password
    print_info "Changing default password..."
    local response=$(curl -s -w "%{http_code}" -X POST \
        -u "$default_user:$default_pass" \
        -d "login=$default_user&password=$new_pass&previousPassword=$default_pass" \
        "$SONAR_HOST_URL/api/users/change_password")
    
    local http_code="${response: -3}"
    
    if [ "$http_code" = "204" ] || [ "$http_code" = "200" ]; then
        print_success "Password changed successfully"
        echo "$default_user:$new_pass"
        return 0
    fi
    
    print_error "Failed to setup credentials"
    return 1
}

# Create or update quality gate
setup_quality_gate() {
    local credentials="$1"
    local gate_name="Wallet API Quality Gate"
    
    print_info "Setting up quality gate with ${REQUIRED_COVERAGE}% coverage requirement..."
    
    # Check if quality gate already exists
    local existing_gate=$(curl -s -u "$credentials" \
        "$SONAR_HOST_URL/api/qualitygates/list" | \
        grep -o "\"name\":\"$gate_name\"" || true)
    
    if [ -n "$existing_gate" ]; then
        print_success "Quality gate already exists"
        return 0
    fi
    
    # Create quality gate
    local create_response=$(curl -s -X POST -u "$credentials" \
        -d "name=$gate_name" \
        "$SONAR_HOST_URL/api/qualitygates/create")
    
    local gate_id=$(echo "$create_response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
    
    if [ -n "$gate_id" ]; then
        # Add coverage condition
        curl -s -X POST -u "$credentials" \
            -d "gateId=$gate_id" \
            -d "metric=coverage" \
            -d "op=LT" \
            -d "error=$REQUIRED_COVERAGE" \
            "$SONAR_HOST_URL/api/qualitygates/create_condition" > /dev/null
        
        # Set as default
        curl -s -X POST -u "$credentials" \
            -d "id=$gate_id" \
            "$SONAR_HOST_URL/api/qualitygates/set_as_default" > /dev/null
        
        print_success "Quality gate created and set as default"
    else
        print_warning "Failed to create quality gate, but continuing..."
    fi
}

# Main execution
main() {
    echo -e "\n${BLUE}🔧 SonarQube Initialization${NC}"
    echo -e "${BLUE}============================${NC}\n"
    
    # Wait for SonarQube
    if ! wait_for_sonarqube; then
        exit 1
    fi
    
    # Setup credentials
    local credentials=$(setup_credentials)
    if [ $? -ne 0 ]; then
        exit 1
    fi
    
    # Setup quality gate
    setup_quality_gate "$credentials"
    
    print_success "SonarQube initialization completed!"
    print_info "Credentials: $credentials"
    print_info "Quality Gate: Coverage >= ${REQUIRED_COVERAGE}%"
    print_info "Dashboard: ${SONAR_HOST_URL}"
}

# Run main function
main "$@"
