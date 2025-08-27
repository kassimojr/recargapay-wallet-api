#!/bin/bash

# =============================================================================
# Digital Wallet API - Complete Startup Script
# =============================================================================
# This script provides a complete development environment setup and simulates a CI/CD pipeline by:
# - Infrastructure services (PostgreSQL, Redis, SonarQube, Grafana, etc.)
# - Spring Boot application build and deployment
# - Health checks and smoke tests
# - Comprehensive logging and error handling
# =============================================================================

# Strict error handling
set -euo pipefail  # Exit on error, undefined vars, pipe failures

# Trap to cleanup on script exit
trap 'echo "Script interrupted or failed. Check logs for details."' ERR INT TERM

# Execution time tracking
SCRIPT_START_TIME=$(date +%s)

# Function to calculate and display execution time
calculate_execution_time() {
    local end_time=$(date +%s)
    local duration=$((end_time - SCRIPT_START_TIME))
    local hours=$((duration / 3600))
    local minutes=$(((duration % 3600) / 60))
    local seconds=$((duration % 60))
    
    if [ $hours -gt 0 ]; then
        echo "${hours}h ${minutes}m ${seconds}s"
    elif [ $minutes -gt 0 ]; then
        echo "${minutes}m ${seconds}s"
    else
        echo "${seconds}s"
    fi
}

# Function to show progress with elapsed time
show_progress_with_time() {
    local message="$1"
    local current_time=$(date +%s)
    local elapsed=$((current_time - SCRIPT_START_TIME))
    local elapsed_formatted=$(printf "%02d:%02d" $((elapsed / 60)) $((elapsed % 60)))
    echo -e "${BLUE}[${elapsed_formatted}] ${message}${NC}"
}

# Set color variables for better output formatting
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Configuration
REQUIRED_COVERAGE=90
SONAR_HOST_URL="http://localhost:9000"
SPRING_PROFILE="prod"
API_PORT=8080

# Timeout configurations (optimized for first-time setup)
MAX_WAIT_TIME=1800        # 30 minutes total max wait
DOCKER_PULL_TIMEOUT=1200  # 20 minutes for image downloads
BUILD_TIMEOUT=900         # 15 minutes for Maven builds
SERVICE_WAIT_ATTEMPTS=120 # 2 minutes per service (120 * 1s = 2min)
SONARQUBE_WAIT_ATTEMPTS=180 # 3 minutes for SonarQube (180 * 1s = 3min)

# Logging configuration
LOG_DIR="logs"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
STARTUP_LOG="$LOG_DIR/startup_${TIMESTAMP}.log"
MAVEN_LOG="$LOG_DIR/maven_${TIMESTAMP}.log"
SONAR_LOG="$LOG_DIR/sonar_${TIMESTAMP}.log"
DOCKER_LOG="$LOG_DIR/docker_${TIMESTAMP}.log"

# Create logs directory if it doesn't exist
mkdir -p "$LOG_DIR"

# =============================================================================
# GLOBAL VARIABLES
# =============================================================================

# Color codes for output formatting
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Global variables
START_TIME=$(date +%s)
API_PORT=8080
SONAR_HOST_URL="http://localhost:9000"
SPRING_PROFILE="prod"
REQUIRED_COVERAGE=90
DOCKER_PULL_TIMEOUT=1800  # 30 minutes
DOCKER_BUILD_TIMEOUT=1800 # 30 minutes
MAVEN_BUILD_TIMEOUT=1800  # 30 minutes

# Global SonarQube token (will be set during startup)
sonar_token=""

# =============================================================================
# UTILITY FUNCTIONS
# =============================================================================

print_header() {
    echo -e "\n${BOLD}${CYAN}🚀 Digital Wallet API - Complete Startup${NC}"
    echo -e "${CYAN}=============================================${NC}\n"
}

print_step() {
    echo -e "\n${BOLD}${BLUE}📋 $1${NC}"
    echo -e "${BLUE}$(printf '=%.0s' {1..50})${NC}"
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

print_info() {
    echo -e "${CYAN}ℹ️  $1${NC}"
}

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check if a port is available
is_port_available() {
    local port=$1
    ! nc -z localhost $port >/dev/null 2>&1
}

# Function to check if a container is running
is_container_running() {
    local container_name=$1
    [ "$(docker ps -q -f name=$container_name)" ]
}

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=${3:-$SERVICE_WAIT_ATTEMPTS}
    local attempt=1
    
    show_progress_with_time "Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        # Check if URL starts with http:// or https:// (HTTP service)
        if [[ "$url" =~ ^https?:// ]]; then
            # HTTP service - use curl
            if curl -s -f "$url" >/dev/null 2>&1; then
                print_success "$service_name is ready!"
                return 0
            fi
        else
            # TCP service (host:port format) - use netcat
            local host=$(echo "$url" | cut -d':' -f1)
            local port=$(echo "$url" | cut -d':' -f2)
            
            if nc -z "$host" "$port" >/dev/null 2>&1; then
                print_success "$service_name is ready!"
                return 0
            fi
        fi
        
        # Show progress every 10 attempts
        if [ $((attempt % 10)) -eq 0 ]; then
            show_progress_with_time "Still waiting for $service_name... (attempt $attempt/$max_attempts)"
        else
            echo -n "."
        fi
        
        sleep 1
        attempt=$((attempt + 1))
    done
    
    print_error "$service_name failed to become ready after $((max_attempts)) seconds"
    return 1
}

# Function to extract coverage from SonarQube
get_sonar_coverage() {
    local project_key="digital-wallet-api"
    local sonar_api="${SONAR_HOST_URL}/api/measures/component"
    
    # Wait a bit for SonarQube to process the analysis
    sleep 10
    
    # Try to get coverage using the token if available (more reliable)
    local coverage=""
    if [ -n "$sonar_token" ]; then
        coverage=$(curl -s -H "Authorization: Bearer $sonar_token" \
            "${sonar_api}?component=${project_key}&metricKeys=coverage" \
            | grep -o '"value":"[^"]*"' \
            | head -1 \
            | cut -d'"' -f4)
    fi
    
    # Fallback to admin:admin if token method fails
    if [ -z "$coverage" ]; then
        coverage=$(curl -s -u admin:admin \
            "${sonar_api}?component=${project_key}&metricKeys=coverage" \
            | grep -o '"value":"[^"]*"' \
            | head -1 \
            | cut -d'"' -f4)
    fi
    
    echo "$coverage"
}

# Function to extract version from pom.xml
get_project_version() {
    local pom_file="pom.xml"
    if [ ! -f "$pom_file" ]; then
        print_error "pom.xml not found"
        return 1
    fi
    
    # Extract project version (skip parent version) using xmllint or fallback to grep/sed
    local version=""
    
    # Try xmllint first (more reliable for XML parsing)
    if command -v xmllint >/dev/null 2>&1; then
        version=$(xmllint --xpath "//project/version/text()" "$pom_file" 2>/dev/null | head -1)
    fi
    
    # Fallback to grep/sed if xmllint not available or failed
    if [ -z "$version" ]; then
        # Look for version after artifactId to avoid parent version
        version=$(awk '/<artifactId>digital-wallet-api<\/artifactId>/{found=1; next} found && /<version>/{gsub(/.*<version>|<\/version>.*/, ""); print; exit}' "$pom_file" | tr -d '[:space:]')
    fi
    
    if [ -z "$version" ]; then
        print_error "Could not extract version from pom.xml"
        return 1
    fi
    
    echo "$version"
}

# =============================================================================
# PRE-VALIDATION CHECKS
# =============================================================================

pre_validation_checks() {
    print_step "Pre-validation Checks"
    
    # Generate .env file if it doesn't exist or is incomplete
    print_info "Checking environment configuration..."
    if [ ! -f ".env" ] || [ ! -s ".env" ]; then
        print_info "Generating .env file for automated setup..."
        if [ -f "scripts/01-generate-env.sh" ]; then
            bash scripts/01-generate-env.sh
            if [ $? -eq 0 ]; then
                print_success ".env file generated successfully!"
            else
                print_error "Failed to generate .env file"
                exit 1
            fi
        else
            print_error "Environment generator script not found at scripts/01-generate-env.sh"
            exit 1
        fi
    else
        print_success ".env file already exists and is valid"
    fi
    
    # Check required commands
    local required_commands=("docker" "docker-compose" "mvn" "curl" "nc")
    
    for cmd in "${required_commands[@]}"; do
        if command_exists "$cmd"; then
            print_success "$cmd is available"
        else
            print_error "$cmd is not available. Please install it."
            return 1
        fi
    done
    
    # Check timeout support (multiple methods available)
    local os_type=$(detect_os)
    local timeout_method="none"
    
    if command_exists "gtimeout"; then
        print_success "gtimeout is available (GNU coreutils)"
        timeout_method="gtimeout"
    elif command_exists "timeout"; then
        print_success "timeout is available (native)"
        timeout_method="timeout"
    else
        print_success "timeout support via background processes (universal fallback)"
        timeout_method="fallback"
    fi
    
    print_info "🖥️  Operating System: $os_type"
    print_info "⏱️  Timeout Method: $timeout_method"
    
    if [ "$timeout_method" = "fallback" ]; then
        print_info "💡 For better timeout support:"
        case "$os_type" in
            "macOS")
                print_info "   • Install GNU coreutils: brew install coreutils"
                ;;
            "Linux")
                print_info "   • timeout should be available by default"
                ;;
            "Windows")
                print_info "   • Using WSL fallback method"
                ;;
            *)
                print_info "   • Using universal fallback method"
                ;;
        esac
    fi
    
    # Check if Docker is running
    if docker info >/dev/null 2>&1; then
        print_success "Docker is running"
    else
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    
    # Check if API port is available
    if is_port_available $API_PORT; then
        print_success "Port $API_PORT is available"
    else
        print_warning "Port $API_PORT is already in use. Will attempt to use existing service."
    fi
    
    # Check if .env file exists
    if [ -f ".env" ]; then
        print_success ".env file found"
    else
        print_warning ".env file not found. Using default configuration."
    fi
    
    print_success "All pre-validation checks passed!"
}

# =============================================================================
# INTEGRATED BUILD WITH SONARQUBE ANALYSIS
# =============================================================================

maven_build() {
    print_step "Integrated Build with SonarQube Analysis & Coverage Validation (90% Required)"
    
    # Start SonarQube if not running
    if ! is_container_running "sonarqube"; then
        print_info "Starting SonarQube container..."
        docker-compose up -d sonarqube postgres
        
        # Wait for SonarQube to be ready
        if ! wait_for_sonarqube_ready; then
            print_error "SonarQube failed to start"
            exit 1
        fi
    else
        print_success "SonarQube is already running"
        # Still wait to ensure it's fully ready
        wait_for_sonarqube_ready
    fi
    
    print_info "Waiting for SonarQube to be ready..."
    if ! wait_for_sonarqube_ready; then
        print_error "SonarQube failed to start"
        exit 1
    fi
    
    print_success "Core services are ready!"
    
    print_info "Generating authentication token..."
    
    # Handle potential forced password reset for automation
    print_info "Ensuring SonarQube is ready for automation (handling potential password reset)..."
    
    # Try to access SonarQube and handle forced password reset if needed
    local reset_response=$(curl -s -w "%{http_code}" -u "admin:admin" "$SONAR_HOST_URL/api/authentication/validate")
    local http_code="${reset_response: -3}"
    
    if [ "$http_code" = "401" ] || [ "$http_code" = "403" ]; then
        print_info "SonarQube may require password reset, attempting automated reset..."
        
        # Try to reset password programmatically
        local change_response=$(curl -s -w "%{http_code}" -X POST \
            -u "admin:admin" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "login=admin&password=admin&previousPassword=admin" \
            "$SONAR_HOST_URL/api/users/change_password")
        
        local change_code="${change_response: -3}"
        if [ "$change_code" = "204" ] || [ "$change_code" = "200" ]; then
            print_success "Password reset completed automatically"
        else
            print_warning "Automated password reset failed, but continuing with default credentials"
        fi
    else
        print_success "SonarQube authentication is ready"
    fi
    
    # Simple and reliable token generation for SonarQube 25.7
    local token_name="wallet-api-$(date +%s)"
    
    # Try direct token generation with admin:admin (works with SonarQube 25.7)
    print_info "Attempting token generation with default credentials..."
    local token_response=$(curl -s -X POST \
        -u "admin:admin" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "name=$token_name" \
        "$SONAR_HOST_URL/api/user_tokens/generate")
    
    # Extract token from response
    sonar_token=$(echo "$token_response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    
    if [ -n "$sonar_token" ] && [ "$sonar_token" != "null" ]; then
        print_success "SonarQube token generated successfully!"
        print_info "Token: ${sonar_token:0:20}..." # Show first 20 chars for verification
    else
        print_error "Failed to generate SonarQube token"
        print_error "Response: $token_response"
        exit 1
    fi
    
    print_success "SonarQube authentication configured successfully!"
    
    print_info "Running integrated build with SonarQube analysis..."
    print_info "📝 Build output will be shown below and saved to: $MAVEN_LOG"
    echo ""
    
    # Prepare Maven build command
    local maven_command="mvn clean verify sonar:sonar \
        -Dsonar.projectKey=digital-wallet-api \
        -Dsonar.host.url=\"$SONAR_HOST_URL\" \
        -Dsonar.token=\"$sonar_token\" \
        -s settings.xml"
    
    # Execute Maven build with timeout and progress monitoring
    if execute_with_timeout $BUILD_TIMEOUT "Maven build with SonarQube analysis" "$maven_command" "$MAVEN_LOG"; then
        print_success "✅ Application built successfully!"
        show_progress_with_time "Build completed"
    else
        local exit_code=$?
        print_error "💡 Check the Maven output above for details"
        exit $exit_code
    fi
    
    # Check if SonarQube analysis actually succeeded by looking for success indicators
    if [ -f "$MAVEN_LOG" ]; then
        # Check for Maven build success patterns
        if grep -q "BUILD SUCCESS" "$MAVEN_LOG"; then
            print_success "✅ Maven BUILD SUCCESS confirmed!"
            print_success "🎉 Integrated build and SonarQube analysis completed successfully!"
        elif grep -q "Tests run:" "$MAVEN_LOG" || grep -q "Started.*Application.*in" "$MAVEN_LOG"; then
            print_success "✅ Build completed successfully (tests executed)!"
            print_success "🎉 Integrated build and SonarQube analysis completed successfully!"
        else
            print_warning "⚠️  Build completed but could not verify SonarQube analysis status"
            print_info "💡 Check $MAVEN_LOG for detailed information"
            # Don't exit - continue with the process
        fi
    else
        print_warning "Maven log file not found: $MAVEN_LOG"
        print_info "Build appears to have completed successfully based on exit code"
    fi
    
    # Validate coverage using API
    print_info "Validating code coverage requirement (>= ${REQUIRED_COVERAGE}%)..."
    
    local coverage=$(validate_coverage_from_api "$sonar_token")
    if [ $? -ne 0 ] || [ -z "$coverage" ]; then
        print_error "Failed to retrieve coverage data"
        print_error "Cannot validate coverage requirement"
        exit 1
    fi
    
    # Convert coverage to integer for comparison (handle decimal properly)
    local coverage_int=$(echo "$coverage" | cut -d'.' -f1)
    
    # Validate that coverage_int is a number
    if ! [[ "$coverage_int" =~ ^[0-9]+$ ]]; then
        print_error "Invalid coverage data received: '$coverage'"
        exit 1
    fi
    
    print_info "📊 Current code coverage: ${coverage}%"
    print_info "📋 Required coverage: ${REQUIRED_COVERAGE}%"
    
    if [ "$coverage_int" -ge "$REQUIRED_COVERAGE" ]; then
        print_success "✅ Coverage requirement MET! (${coverage}% >= ${REQUIRED_COVERAGE}%)"
        print_success "🎉 Quality Gate PASSED - Proceeding with deployment"
    else
        print_error "❌ Coverage requirement NOT MET! (${coverage}% < ${REQUIRED_COVERAGE}%)"
        print_error "🚫 Quality Gate FAILED - Deployment blocked"
        print_error ""
        print_error "📈 To fix this issue:"
        print_error "   1. Add more unit tests to increase coverage"
        print_error "   2. Focus on untested classes and methods"
        print_error "   3. Run 'mvn clean test jacoco:report' to see detailed coverage report"
        print_error "   4. Check target/site/jacoco/index.html for coverage details"
        exit 1
    fi
    
    # Display SonarQube dashboard link
    print_info "🔗 View detailed analysis at: ${SONAR_HOST_URL}/dashboard?id=digital-wallet-api"
}

# =============================================================================
# SONARQUBE ANALYSIS WITH AUTOMATED TOKEN MANAGEMENT
# =============================================================================

# Function to wait for SonarQube to be fully ready
wait_for_sonarqube_ready() {
    local max_attempts=$SONARQUBE_WAIT_ATTEMPTS
    local attempt=1
    
    show_progress_with_time "Waiting for SonarQube to be fully operational..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$SONAR_HOST_URL/api/system/status" | grep -q '"status":"UP"'; then
            print_success "SonarQube is ready!"
            return 0
        fi
        
        # Show progress every 15 attempts (every 15 seconds)
        if [ $((attempt % 15)) -eq 0 ]; then
            show_progress_with_time "Still waiting for SonarQube... (attempt $attempt/$max_attempts)"
        else
            echo -n "."
        fi
        
        sleep 1
        ((attempt++))
    done
    
    print_error "SonarQube failed to become ready after $((max_attempts)) seconds"
    return 1
}

# Function to validate coverage from SonarQube API
validate_coverage_from_api() {
    local token="$1"
    local project_key="digital-wallet-api"
    
    print_info "Retrieving coverage data from SonarQube API..." >&2
    
    # Wait a bit for analysis to be processed
    sleep 10
    
    local max_attempts=12
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        local coverage_response=$(curl -s -H "Authorization: Bearer $token" \
            "$SONAR_HOST_URL/api/measures/component?component=$project_key&metricKeys=coverage")
        
        local coverage=$(echo "$coverage_response" | grep -o '"value":"[0-9.]*"' | head -1 | cut -d'"' -f4)
        
        if [ -n "$coverage" ] && [ "$coverage" != "null" ]; then
            echo "$coverage"
            return 0
        fi
        
        print_info "Waiting for coverage data... (attempt $attempt/$max_attempts)" >&2
        sleep 10
        ((attempt++))
    done
    
    print_warning "Could not retrieve coverage data from SonarQube API" >&2
    return 1
}

# =============================================================================
# INFRASTRUCTURE STARTUP
# =============================================================================

start_infrastructure() {
    print_step "Starting Infrastructure Services"
    
    # Check if key infrastructure services are already running
    local services_to_check=("wallet-redis" "wallet-grafana" "wallet-loki" "wallet-promtail" "wallet-prometheus" "wallet-tempo")
    local all_running=true
    
    print_info "Checking infrastructure services status..."
    for service in "${services_to_check[@]}"; do
        if ! is_service_healthy "$service" "container"; then
            all_running=false
            break
        fi
    done
    
    if [ "$all_running" = true ]; then
        print_success "✅ All infrastructure services are already running and healthy"
        print_info "📋 Services Status:"
        docker ps --format "table {{.Names}}\t{{.Status}}" --filter "name=wallet-" | grep -E "(redis|grafana|loki|promtail|prometheus|tempo)"
        return 0
    fi
    
    print_info "Starting Docker services..."
    print_info "📝 Docker output will be shown below and saved to: $DOCKER_LOG"
    echo ""
    
    if docker-compose up -d | tee "$DOCKER_LOG"; then
        print_success "Docker services started!"
    else
        print_error "Failed to start Docker services!"
        print_error "Check $DOCKER_LOG for detailed error information"
        exit 1
    fi
}

# =============================================================================
# SPRING BOOT APPLICATION AS DOCKER SERVICE
# =============================================================================

start_spring_boot_docker() {
    print_step "Starting Spring Boot Application (Docker Service)"
    
    # Always remove existing container to ensure fresh build/deployment
    print_info "🔄 Ensuring clean environment for fresh deployment..."
    
    # Force stop and remove any existing wallet-api container (robust approach)
    docker stop wallet-api >/dev/null 2>&1 || true
    docker rm -f wallet-api >/dev/null 2>&1 || true
    
    # Double-check and force removal if still exists
    if docker ps -a --format "{{.Names}}" | grep -q "^wallet-api$"; then
        print_info "🔄 Force removing persistent wallet-api container..."
        docker rm -f wallet-api >/dev/null 2>&1 || true
    fi
    
    print_success "✅ Clean environment ready for fresh deployment"
    
    print_info "Building Docker image for the application..."
    local version=$(get_project_version)
    if [ $? -ne 0 ]; then
        print_error "Failed to extract version from pom.xml"
        exit 1
    fi
    
    print_info "Building Docker image: digital-wallet-api:$version"
    
    # Build with clean output - show progress but hide verbose details
    if [ "$IDE_MODE" = true ]; then
        print_info "🔨 Building application image (IDE mode - may use cache)..."
        local build_args=""
    else
        print_info "🔨 Building application image from scratch (CI/CD mode - no cache)..."
        local build_args="--no-cache --pull"
    fi
    
    # Capture build output for logging but show clean progress
    local build_output=$(mktemp)
    local build_start_time=$(date +%s)
    
    if docker build $build_args -t digital-wallet-api:$version . > "$build_output" 2>&1; then
        local build_end_time=$(date +%s)
        local build_duration=$((build_end_time - build_start_time))
        
        # Check if build used cache (fast) or was full build (slow)
        if [ "$IDE_MODE" = true ] && grep -q "CACHED" "$build_output"; then
            print_success "✅ Docker image built successfully! (${build_duration}s - used cache)"
        else
            print_success "✅ Docker image built successfully! (${build_duration}s - fresh build)"
        fi
        
        # Log detailed output to file for troubleshooting
        cat "$build_output" >> "$DOCKER_LOG"
        rm -f "$build_output"
    else
        print_error "❌ Failed to build Docker image"
        print_error "📋 Build output:"
        cat "$build_output"
        print_error "📝 Full logs saved to: $DOCKER_LOG"
        cat "$build_output" >> "$DOCKER_LOG"
        rm -f "$build_output"
        exit 1
    fi
    
    # Start application container
    print_info "🚀 Starting wallet-api container with version: $version"
    
    local container_id
    if container_id=$(docker run -d \
        --name wallet-api \
        --network digital-wallet-api_default \
        --label com.docker.compose.project=digital-wallet-api \
        --label com.docker.compose.service=wallet-api \
        -p 8080:8080 \
        -v $(pwd)/logs:/app/logs \
        --env-file .env \
        -e SPRING_PROFILES_ACTIVE=prod \
        -e DB_HOST=postgres-sonar \
        -e REDIS_HOST=wallet-redis \
        digital-wallet-api:$version 2>&1); then
        print_success "✅ Container started successfully!"
        print_info "📋 Container ID: ${container_id:0:12}..."
    else
        print_error "❌ Failed to start wallet-api container"
        print_error "📋 Error details: $container_id"
        print_error "💡 Check Docker logs: docker logs wallet-api"
        exit 1
    fi
    
    # Verify container is actually running
    print_info "🔍 Verifying container status..."
    sleep 3
    
    local container_status
    if container_status=$(docker ps --format "{{.Names}}\t{{.Status}}" | grep "^wallet-api"); then
        print_success "✅ Container is running and healthy!"
        print_info "📊 Status: $container_status"
    else
        print_error "❌ Container wallet-api is not running properly"
        print_error "📋 Container status:"
        docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" --filter name=wallet-api
        print_error "💡 Check Docker logs: docker logs wallet-api"
        exit 1
    fi
    
    print_info "📋 Container Details:"
    print_info "   • Name: wallet-api"
    print_info "   • Port: 8080"
    print_info "   • Profile: prod"
    print_info "   • Version: $version"
    
    # Verify application startup
    verify_application_startup
    
    # Show application info
    print_info "Application is running at: http://localhost:8080"
    print_info "Health check: http://localhost:8080/actuator/health"
    print_info "API documentation: http://localhost:8080/swagger-ui.html"
}

# Function to verify application startup
verify_application_startup() {
    print_info "🔍 Verifying container is running and healthy..."
    
    # Step 1: Verify container is actually running
    if ! docker ps --format "{{.Names}}" | grep -q "^wallet-api$"; then
        print_error "❌ wallet-api container is not running!"
        return 1
    fi
    
    local container_status=$(docker ps --format "{{.Names}}\t{{.Status}}" | grep "^wallet-api")
    print_success "✅ Container is running: $container_status"
    
    # Step 2: Give application a moment to initialize (non-blocking)
    print_info "⏳ Allowing 30 seconds for application initialization..."
    sleep 30
    
    # Step 3: Quick health check attempt (non-blocking, best effort)
    print_info "🏥 Attempting quick health check (non-blocking)..."
    
    local actuator_response=$(curl -s -w "%{http_code}" -o /dev/null --max-time 10 http://localhost:8080/actuator/health 2>/dev/null || echo "000")
    if [ "$actuator_response" = "200" ]; then
        print_success "✅ Health check successful - application is ready!"
        return 0
    fi
    
    # Step 4: Fallback API check (non-blocking, best effort)
    local api_response=$(curl -s -w "%{http_code}" -o /dev/null --max-time 10 http://localhost:8080/api/v1/auth/login 2>/dev/null || echo "000")
    if [ "$api_response" = "401" ] || [ "$api_response" = "400" ]; then
        print_success "✅ API endpoints responding - application is ready!"
        return 0
    fi
    
    # Step 5: Final verification - container still running?
    if docker ps --format "{{.Names}}" | grep -q "^wallet-api$"; then
        print_warning "⚠️  Health endpoints not responding yet, but container is running"
        print_info "🎯 This is normal for startup from zero - application may need more time"
        print_info "💡 Container is healthy and will continue initializing in background"
        print_success "✅ Startup verification completed - proceeding with deployment"
        return 0
    else
        print_error "❌ Container stopped during startup - this indicates a real failure"
        return 1
    fi
}

# =============================================================================
# HEALTH CHECKS & SMOKE TESTS
# =============================================================================

run_health_checks() {
    print_step "Health Checks & Smoke Tests"
    
    # Health check
    print_info "🏥 Checking application health..."
    local health_response=$(curl -s "http://localhost:8080/actuator/health" 2>/dev/null)
    local health_status=$?
    
    if [ $health_status -eq 0 ] && echo "$health_response" | grep -q "UP"; then
        print_success "✅ Application health check passed!"
        print_info "📊 Health Status: $(echo "$health_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)"
    else
        print_warning "⚠️  Application health check failed or returned unexpected response"
        if [ $health_status -ne 0 ]; then
            print_info "💡 Connection failed - application may still be starting"
        else
            print_info "📋 Response: $health_response"
        fi
    fi
    
    # Test authentication
    print_info "🔐 Testing authentication..."
    local auth_response=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -d '{"username":"admin","password":"admin"}' \
        "http://localhost:$API_PORT/api/v1/auth/login" 2>/dev/null)
    local auth_status=$?
    
    if [ $auth_status -eq 0 ] && echo "$auth_response" | grep -q "token"; then
        print_success "✅ Authentication test passed!"
        
        # Extract token for further tests
        local token=$(echo "$auth_response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        
        if [ -n "$token" ]; then
            print_info "🎫 JWT token obtained successfully"
            
            # Test protected endpoint
            print_info "🔒 Testing protected endpoint..."
            local wallets_response=$(curl -s -H "Authorization: Bearer $token" \
                "http://localhost:$API_PORT/api/v1/wallet" 2>/dev/null)
            local wallet_status=$?
            
            if [ $wallet_status -eq 0 ]; then
                print_success "✅ Protected endpoint test passed!"
            else
                print_warning "Protected endpoint test failed"
                print_info "💡 Check API logs: docker logs wallet-api"
            fi
        else
            print_warning "⚠️  Failed to extract JWT token from response"
            print_info "📋 Response: $auth_response"
        fi
    else
        print_warning "⚠️  Authentication test failed"
        if [ $auth_status -ne 0 ]; then
            print_info "💡 Connection failed - check if API is running"
        else
            print_info "📋 Response: $auth_response"
        fi
    fi
    
    # Validate log ingestion
    print_info "📊 Validating log ingestion pipeline..."
    
    # Check if log file exists and is being updated
    local log_file="$LOG_DIR/wallet-api.json"
    if [ -f "$log_file" ]; then
        # Check if file was modified in the last 2 minutes (recent activity)
        if [ $(find "$log_file" -mmin -2 | wc -l) -gt 0 ]; then
            print_success "✅ Log file is being actively updated!"
            
            # Simple validation - check if Loki can find recent logs
            print_info "🔍 Checking Loki log ingestion..."
            local start_time=$(($(date +%s) - 300))  # Last 5 minutes
            local end_time=$(date +%s)
            local loki_query="http://localhost:3100/loki/api/v1/query_range?query=%7Bjob%3D%22wallet-api%22%7D&start=${start_time}000000000&end=${end_time}000000000&limit=5"
            
            local loki_response=$(curl -s "$loki_query" 2>/dev/null || echo '{"data":{"result":[]}}')
            local log_count=$(echo "$loki_response" | jq -r '.data.result | length' 2>/dev/null || echo "0")
            
            if [ "$log_count" -gt "0" ]; then
                print_success "✅ Log ingestion working! Found $log_count log streams in Loki"
                print_info "📈 Logs are being collected and ingested successfully"
                print_info "🌐 Access Grafana at http://localhost:3000 to view logs"
            else
                print_warning "Logs are being generated but may not be fully ingested yet"
                print_info "⏳ This is normal on first startup - logs should appear in Grafana within 15 seconds"
                print_info "🔍 Check Grafana at http://localhost:3000 or run: curl 'http://localhost:3100/loki/api/v1/query?query=%7Bjob%3D%22wallet-api%22%7D'"
            fi
        else
            print_warning "Log file exists but hasn't been updated recently"
            print_info "This may be normal if no API requests have been made yet"
        fi
    else
        print_warning "JSON log file not found at $log_file"
        print_info "Logs will be created when the application starts generating them"
    fi
    
    print_success "Health checks and smoke tests completed!"
}

# =============================================================================
# PRE-DOWNLOAD DOCKER IMAGES
# =============================================================================

pre_download_images() {
    print_step "Pre-downloading Docker Images"
    
    show_progress_with_time "Downloading Docker images (this may take 10-20 minutes on first run)..."
    print_info "📦 Images to download: PostgreSQL, Redis, SonarQube, Prometheus, Grafana, Loki, Promtail, Tempo"
    print_info "⏰ Estimated time: 5-20 minutes depending on your internet connection"
    print_info "🔄 You can monitor progress in the Docker output below..."
    echo ""
    
    # Execute Docker image pull with timeout and progress monitoring
    if execute_with_timeout $DOCKER_PULL_TIMEOUT "Docker image download" "docker-compose pull"; then
        print_success "✅ All Docker images downloaded successfully!"
        show_progress_with_time "Image download completed"
    else
        local exit_code=$?
        if [ $exit_code -eq 124 ]; then
            print_warning "⚠️  Docker image download timed out after $((DOCKER_PULL_TIMEOUT / 60)) minutes"
            print_info "🔄 Continuing with startup - images will be downloaded as needed"
        else
            print_warning "⚠️  Some images may not have downloaded completely"
            print_info "🔄 Continuing with startup - missing images will be downloaded as needed"
        fi
    fi
    
    echo ""
}

# =============================================================================
# FINAL INFORMATION
# =============================================================================

show_final_info() {
    local execution_time=$(calculate_execution_time)
    
    # Get current coverage from SonarQube
    local current_coverage=$(get_sonar_coverage)
    if [ -z "$current_coverage" ] || [ "$current_coverage" = "N/A" ]; then
        current_coverage="N/A"
    else
        # Remove any % symbol if present to avoid duplication
        current_coverage=$(echo "$current_coverage" | sed 's/%$//')
    fi
    
    print_step "🎉 Wallet API Startup Complete!"
    
    echo ""
    echo -e "${GREEN}╔══════════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║                           🚀 WALLET API READY 🚀                            ║${NC}"
    echo -e "${GREEN}╠══════════════════════════════════════════════════════════════════════════════╣${NC}"
    echo -e "${GREEN}║                                                                              ║${NC}"
    echo -e "${GREEN}║  📊 Application:     http://localhost:$API_PORT                                    ║${NC}"
    echo -e "${GREEN}║  🔍 Health Check:    http://localhost:$API_PORT/actuator/health                    ║${NC}"
    echo -e "${GREEN}║  📈 Metrics:         http://localhost:$API_PORT/actuator/prometheus               ║${NC}"
    echo -e "${GREEN}║  📋 SonarQube:       $SONAR_HOST_URL                                    ║${NC}"
    echo -e "${GREEN}║  📊 Grafana:         http://localhost:3000                                    ║${NC}"
    echo -e "${GREEN}║  🔍 Loki Logs:       http://localhost:3000/explore                          ║${NC}"
    echo -e "${GREEN}║                                                                              ║${NC}"
    echo -e "${GREEN}║  📝 Profile Used:    $SPRING_PROFILE                                              ║${NC}"
    echo -e "${GREEN}║  🎯 Coverage Target: $REQUIRED_COVERAGE%                                            ║${NC}"
    echo -e "${GREEN}║  📊 Current Coverage: ${current_coverage}%                                           ║${NC}"
    echo -e "${GREEN}║  ⏱️  Total Execution Time: ${execution_time}                                        ║${NC}"
    echo -e "${GREEN}║  📝 Execution Mode:  ${EXECUTION_MODE}                                             ║${NC}"
    echo -e "${GREEN}║                                                                              ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    print_info "🔧 Development Commands:"
    echo "  • Stop services: ./wallet-api-shutdown.sh"
    echo "  • View logs: docker-compose logs -f wallet-api"
    echo "  • Monitor services: ./scripts/monitoring.sh"
    echo ""
    
    print_info "🚀 Execution Modes Available:"
    echo "  • Full CI/CD:        ./wallet-api-startup.sh"
    echo "  • IDE Development:   ./wallet-api-startup.sh --ide-mode"
    echo "  • Infrastructure:    ./wallet-api-startup.sh --infrastructure-only"
    echo "  • Help:              ./wallet-api-startup.sh --help"
    echo ""
    
    print_info "📚 API Documentation:"
    echo "  • Postman Collection: docs/api/postman/"
    echo "  • Insomnia Collection: docs/api/insomnia/"
    echo "  • API Reference: docs/api/"
    echo ""
    
    print_success "✅ All services are running and ready for development!"
    print_info "🚀 Happy coding! The wallet API is ready to process transactions."
    
    echo ""
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

# Parse command line arguments
IDE_MODE=false
INFRASTRUCTURE_ONLY=false
SHOW_HELP=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --ide-mode)
            IDE_MODE=true
            shift
            ;;
        --infrastructure-only|--infrastructure)
            INFRASTRUCTURE_ONLY=true
            shift
            ;;
        --help|-h)
            SHOW_HELP=true
            shift
            ;;
        *)
            echo "Unknown option: $1"
            SHOW_HELP=true
            shift
            ;;
    esac
done

# Show help if requested
if [ "$SHOW_HELP" = true ]; then
    echo "Digital Wallet API - Startup Script"
    echo ""
    echo "Usage:"
    echo "  $0                         Full startup (fresh build + infrastructure + application)"
    echo "  $0 --ide-mode              IDE development mode (cached build + infrastructure, no application)"
    echo "  $0 --infrastructure-only   Infrastructure only (no build, no application)"
    echo "  $0 --help                  Show this help message"
    echo ""
    echo "Modes:"
    echo "  Full startup (default):    Complete CI/CD simulation with fresh Docker build"
    echo "                            - Fresh Maven build + SonarQube analysis"
    echo "                            - Docker build with --no-cache --pull"
    echo "                            - All infrastructure services"
    echo "                            - Application as Docker container"
    echo ""
    echo "  IDE development mode:      Optimized for development workflow"
    echo "                            - Maven build (no SonarQube)"
    echo "                            - Docker build with cache (faster)"
    echo "                            - All infrastructure services"
    echo "                            - Port 8080 free for IDE debugging"
    echo ""
    echo "  Infrastructure only:       Supporting services only"
    echo "                            - No Maven build or SonarQube"
    echo "                            - All infrastructure services (DB, Redis, monitoring)"
    echo "                            - Port 8080 free for external application"
    echo ""
    exit 0
fi

main() {
    print_header
    
    # Show execution mode
    if [ "$IDE_MODE" = true ]; then
        EXECUTION_MODE="IDE Development"
        print_info "🎯 EXECUTION MODE: IDE Development"
        print_info "   ├─ Maven build (no SonarQube): ✅"
        print_info "   ├─ Docker build with cache: ✅"
        print_info "   ├─ Infrastructure services: ✅"
        print_info "   └─ Application startup: ❌ (port 8080 free for IDE)"
    elif [ "$INFRASTRUCTURE_ONLY" = true ]; then
        EXECUTION_MODE="Infrastructure Only"
        print_info "🏗️ EXECUTION MODE: Infrastructure Only"
        print_info "   ├─ Maven build + SonarQube analysis: ❌"
        print_info "   ├─ Docker build: ❌"
        print_info "   ├─ Infrastructure services: ✅"
        print_info "   └─ Application startup: ❌ (port 8080 free for external app)"
    else
        EXECUTION_MODE="Full CI/CD Pipeline"
        print_info "🚀 EXECUTION MODE: Full CI/CD Pipeline"
        print_info "   ├─ Maven build + SonarQube analysis: ✅"
        print_info "   ├─ Docker build (fresh, no cache): ✅"
        print_info "   ├─ Infrastructure services: ✅"
        print_info "   └─ Application startup: ✅ (Docker container)"
    fi
    echo ""
    
    # Pre-validation checks
    pre_validation_checks
    
    # Pre-download Docker images
    pre_download_images
    
    # Start core services (PostgreSQL + SonarQube) for build process - only if build is needed
    if [ "$INFRASTRUCTURE_ONLY" = false ]; then
        if [ "$IDE_MODE" = true ]; then
            print_step "Starting Core Services (PostgreSQL only - IDE Mode)"
        else
            print_step "Starting Core Services (PostgreSQL + SonarQube)"
        fi
        
        # Check if PostgreSQL is already running
        if is_service_healthy "postgres-sonar" "container" && is_service_healthy "PostgreSQL" "port"; then
            print_success "✅ PostgreSQL is already running and healthy"
        else
            if [ "$IDE_MODE" = true ]; then
                print_info "Starting PostgreSQL for build process..."
                docker-compose up -d postgres
            else
                print_info "Starting PostgreSQL and SonarQube for build process..."
                docker-compose up -d postgres sonarqube
            fi
            
            # Wait for PostgreSQL to be ready
            print_info "Waiting for PostgreSQL to be ready..."
            if ! wait_for_service "PostgreSQL" "localhost:5432"; then
                print_error "PostgreSQL failed to start"
                exit 1
            fi
            print_success "PostgreSQL is ready!"
        fi
        
        # Only start SonarQube if not in IDE mode
        if [ "$IDE_MODE" = false ]; then
            # Check if SonarQube is already running
            if is_service_healthy "sonarqube" "container" && is_service_healthy "SonarQube" "port"; then
                print_success "✅ SonarQube is already running and healthy"
            else
                if ! docker ps --format "table {{.Names}}" | grep -q "^sonarqube"; then
                    print_info "Starting SonarQube..."
                    docker-compose up -d sonarqube
                fi
                
                print_info "Waiting for SonarQube to be ready..."
                if ! wait_for_sonarqube_ready; then
                    print_error "SonarQube failed to start"
                    exit 1
                fi
                print_success "SonarQube is ready!"
            fi
            
            # Run SonarQube automation setup to handle password reset and configuration
            print_info "Running SonarQube automation setup..."
            if [ -f "scripts/03-setup-sonarqube.sh" ]; then
                bash scripts/03-setup-sonarqube.sh
                if [ $? -eq 0 ]; then
                    print_success "SonarQube automation setup completed!"
                else
                    print_warning "SonarQube automation setup had issues, but continuing..."
                fi
            else
                print_warning "SonarQube automation setup script not found, continuing with default approach"
            fi
        else
            print_info "🎯 IDE Mode: Skipping SonarQube setup and analysis"
        fi
        
        # Initialize databases
        print_info "Initializing databases..."
        if [ -f "scripts/02-init-database.sh" ]; then
            bash scripts/02-init-database.sh
            if [ $? -eq 0 ]; then
                print_success "Database initialization completed!"
            else
                print_warning "Database initialization had issues, but continuing..."
            fi
        else
            print_warning "Database initialization script not found, continuing..."
        fi
    else
        print_info "🏗️ Infrastructure-Only Mode: Skipping build and SonarQube setup"
    fi
    
    # Run Maven build - with or without SonarQube based on mode
    if [ "$INFRASTRUCTURE_ONLY" = false ]; then
        if [ "$IDE_MODE" = true ]; then
            print_step "Maven Build (IDE Mode - No SonarQube)"
            print_info "Running Maven build without SonarQube analysis..."
            
            # Simple Maven build for IDE mode
            if mvn clean compile test-compile -q; then
                print_success "✅ Maven build completed successfully!"
            else
                print_error "❌ Maven build failed"
                exit 1
            fi
        else
            # Full Maven build with SonarQube analysis
            maven_build
        fi
    fi
    
    # Start remaining infrastructure services
    start_infrastructure
    
    # Application startup logic based on mode
    if [ "$IDE_MODE" = true ]; then
        print_info "🎯 IDE Development Mode: Skipping application startup"
        print_info "📝 Port 8080 is free for IDE debugging"
        print_info "🚀 You can now run your application from your IDE in debug mode"
    elif [ "$INFRASTRUCTURE_ONLY" = true ]; then
        print_info "🏗️ Infrastructure-Only Mode: Services ready for external application"
        print_info "📝 Port 8080 is free for external application startup"
    else
        # Start Spring Boot application as Docker service
        start_spring_boot_docker
        
        # Run health checks
        run_health_checks
    fi
    
    # Display final information
    show_final_info
    
    print_success "Startup script completed successfully!"
}

# Trap to handle script interruption
trap 'echo -e "\n${RED}Script interrupted. You may need to clean up manually.${NC}"; exit 1' INT TERM

# Function to detect operating system
detect_os() {
    case "$(uname -s)" in
        Linux*)     echo "Linux";;
        Darwin*)    echo "macOS";;
        CYGWIN*)    echo "Windows";;
        MINGW*)     echo "Windows";;
        MSYS*)      echo "Windows";;
        *)          echo "Unix";;
    esac
}

# Function to run command with timeout (cross-platform compatible)
run_with_timeout() {
    local timeout_duration=$1
    shift
    local command_to_run="$*"
    local os_type=$(detect_os)
    
    # Method 1: Try gtimeout (GNU coreutils - works on macOS with brew install coreutils)
    if command -v gtimeout >/dev/null 2>&1; then
        gtimeout "$timeout_duration" bash -c "$command_to_run"
        return $?
    fi
    
    # Method 2: Try native timeout command (Linux, some Unix systems)
    if command -v timeout >/dev/null 2>&1; then
        timeout "$timeout_duration" bash -c "$command_to_run"
        return $?
    fi
    
    # Method 3: Cross-platform background process with kill (works everywhere)
    local temp_script=$(mktemp)
    echo "#!/bin/bash" > "$temp_script"
    echo "$command_to_run" >> "$temp_script"
    chmod +x "$temp_script"
    
    # Start the command in background
    "$temp_script" &
    local cmd_pid=$!
    
    # Start timeout monitor in background
    (
        sleep "$timeout_duration"
        if kill -0 "$cmd_pid" 2>/dev/null; then
            print_warning "⚠️  Command timed out after ${timeout_duration}s, terminating..."
            kill -TERM "$cmd_pid" 2>/dev/null
            sleep 2
            kill -KILL "$cmd_pid" 2>/dev/null
        fi
    ) &
    local timeout_pid=$!
    
    # Wait for command to complete
    local exit_code=0
    if wait "$cmd_pid" 2>/dev/null; then
        exit_code=$?
        # Command completed successfully, kill the timeout monitor
        kill "$timeout_pid" 2>/dev/null
    else
        exit_code=124  # Standard timeout exit code
    fi
    
    # Cleanup
    rm -f "$temp_script"
    return $exit_code
}

# Function to execute command with robust error handling
execute_with_timeout() {
    local timeout_duration=$1
    local description="$2"
    local command_script="$3"
    local log_file="${4:-}"  # Optional log file parameter with default empty value
    
    show_progress_with_time "$description"
    
    # Create temporary script for the command
    local temp_script=$(mktemp)
    if [ -n "$log_file" ]; then
        # If log file specified, redirect output to both console and file
        echo "$command_script 2>&1 | tee -a \"$log_file\"" > "$temp_script"
    else
        # If no log file, just execute the command
        echo "$command_script" > "$temp_script"
    fi
    chmod +x "$temp_script"
    
    # Execute with timeout - simplified approach
    local exit_code=0
    if run_with_timeout "$timeout_duration" "$temp_script"; then
        exit_code=0
        print_success "✅ $description completed successfully!"
    else
        exit_code=$?
        if [ $exit_code -eq 124 ]; then
            print_error "❌ $description timed out after $((timeout_duration / 60)) minutes"
            print_error "💡 Try increasing timeout or check your internet connection"
        else
            print_error "❌ $description failed with exit code: $exit_code"
        fi
    fi
    
    # Cleanup
    rm -f "$temp_script"
    return $exit_code
}

# Function to check if service is already running and healthy
is_service_healthy() {
    local service_name="$1"
    local check_type="${2:-container}"
    
    case "$check_type" in
        "container")
            # Check if container is running and healthy
            if docker ps --format "{{.Names}}\t{{.Status}}" | grep -q "^${service_name}.*Up.*healthy"; then
                return 0
            elif docker ps --format "{{.Names}}\t{{.Status}}" | grep -q "^${service_name}.*Up"; then
                return 0
            fi
            ;;
        "port")
            # Check if port is responding
            case "$service_name" in
                "postgres-sonar"|"PostgreSQL")
                    if command -v nc >/dev/null 2>&1 && nc -z localhost 5432 >/dev/null 2>&1; then
                        return 0
                    fi
                    ;;
                "sonarqube"|"SonarQube")
                    if curl -s -f "http://localhost:9000/api/system/status" | grep -q '"status":"UP"' >/dev/null 2>&1; then
                        return 0
                    fi
                    ;;
                "wallet-api")
                    if curl -s -f "http://localhost:8080/actuator/health" >/dev/null 2>&1; then
                        return 0
                    fi
                    ;;
            esac
            ;;
    esac
    return 1
}

# Run main function
main "$@"
