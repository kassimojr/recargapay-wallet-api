#!/bin/bash

# Common utilities for RecargaPay Wallet API scripts
# This file contains shared functions used across multiple scripts

# Color variables for consistent output formatting
export RED='\033[0;31m'
export GREEN='\033[0;32m'
export YELLOW='\033[1;33m'
export BLUE='\033[0;34m'
export PURPLE='\033[0;35m'
export CYAN='\033[0;36m'
export WHITE='\033[1;37m'
export NC='\033[0m' # No Color

# Standard logging functions
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

print_step() {
    echo -e "\n${PURPLE}🔧 $1${NC}"
    echo -e "${PURPLE}$(printf '=%.0s' {1..50})${NC}"
}

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check if a port is in use
is_port_in_use() {
    local port=$1
    if command_exists lsof; then
        lsof -i :$port >/dev/null 2>&1
    elif command_exists netstat; then
        netstat -an | grep :$port >/dev/null 2>&1
    else
        # Fallback using nc (netcat)
        nc -z localhost $port >/dev/null 2>&1
    fi
}

# Function to check if a container is running
is_container_running() {
    local container_name=$1
    if [ "$(docker ps -q -f name=$container_name)" ]; then
        return 0 # Container is running
    else
        return 1 # Container is not running
    fi
}

# Function to wait for a service to be ready
wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=${3:-30}
    local attempt=1
    
    print_info "Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$url" >/dev/null 2>&1; then
            print_success "$service_name is ready!"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_error "$service_name failed to become ready after $max_attempts attempts"
    return 1
}

# Function to validate environment file
validate_env_file() {
    local env_file=${1:-.env}
    
    if [ ! -f "$env_file" ]; then
        print_warning "Environment file $env_file not found"
        return 1
    fi
    
    # Check for required variables (customize as needed)
    local required_vars=("POSTGRES_DB" "POSTGRES_USER" "POSTGRES_PASSWORD")
    local missing_vars=()
    
    for var in "${required_vars[@]}"; do
        if ! grep -q "^$var=" "$env_file"; then
            missing_vars+=("$var")
        fi
    done
    
    if [ ${#missing_vars[@]} -gt 0 ]; then
        print_warning "Missing required environment variables: ${missing_vars[*]}"
        return 1
    fi
    
    print_success "Environment file $env_file is valid"
    return 0
}

# Function to backup a file before modification
backup_file() {
    local file=$1
    local backup_suffix=${2:-".backup.$(date +%Y%m%d_%H%M%S)"}
    
    if [ -f "$file" ]; then
        cp "$file" "$file$backup_suffix"
        print_info "Backup created: $file$backup_suffix"
    fi
}

# Function to check Docker and Docker Compose availability
check_docker_requirements() {
    if ! command_exists docker; then
        print_error "Docker is not installed or not in PATH"
        return 1
    fi
    
    if ! command_exists docker-compose && ! docker compose version >/dev/null 2>&1; then
        print_error "Docker Compose is not installed or not available"
        return 1
    fi
    
    if ! docker info >/dev/null 2>&1; then
        print_error "Docker daemon is not running"
        return 1
    fi
    
    print_success "Docker requirements are satisfied"
    return 0
}

# Function to get script directory (useful for relative paths)
get_script_dir() {
    echo "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
}

# Function to get project root directory
get_project_root() {
    local script_dir=$(get_script_dir)
    echo "$(cd "$script_dir/../.." && pwd)"
}

# Function to load environment variables from .env file
load_env_file() {
    local env_file=${1:-.env}
    local project_root=$(get_project_root)
    local full_path="$project_root/$env_file"
    
    if [ -f "$full_path" ]; then
        print_info "Loading environment from $env_file"
        set -a  # Automatically export variables
        source "$full_path"
        set +a  # Stop auto-export
        return 0
    else
        print_warning "Environment file $env_file not found at $full_path"
        return 1
    fi
}

# Function to check if script is run from project root
ensure_project_root() {
    if [ ! -f "pom.xml" ] || [ ! -f "docker-compose.yml" ]; then
        print_error "This script must be run from the project root directory"
        print_info "Please run: cd /path/to/recargapay-wallet-api && $0"
        exit 1
    fi
}

# Function to handle script exit codes consistently
handle_exit() {
    local exit_code=$1
    local step_name=$2
    
    if [ $exit_code -eq 0 ]; then
        print_success "$step_name completed successfully"
    else
        print_error "$step_name failed with exit code $exit_code"
        exit $exit_code
    fi
}

# Function to run a command with proper logging
run_with_logging() {
    local description=$1
    shift
    local command="$@"
    
    print_info "Running: $description"
    if eval "$command"; then
        print_success "$description completed"
        return 0
    else
        local exit_code=$?
        print_error "$description failed"
        return $exit_code
    fi
}

# Export functions for use in other scripts
export -f print_info print_success print_warning print_error print_step
export -f command_exists is_port_in_use is_container_running wait_for_service
export -f validate_env_file backup_file check_docker_requirements
export -f get_script_dir get_project_root load_env_file ensure_project_root
export -f handle_exit run_with_logging
