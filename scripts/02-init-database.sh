#!/bin/bash

# Database Initialization Script
# This script handles PostgreSQL database setup for the RecargaPay Wallet API

# Load common utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/utils/common.sh"

# Ensure we're in the project root
ensure_project_root

print_step "Database Initialization"

# Check Docker requirements
if ! check_docker_requirements; then
    exit 1
fi

# Load environment variables
load_env_file

# Database configuration
DB_HOST="${POSTGRES_HOST:-localhost}"
DB_PORT="${POSTGRES_PORT:-5432}"
DB_NAME="${POSTGRES_DB:-walletdb}"
DB_USER="${POSTGRES_USER:-admin}"
DB_PASSWORD="${POSTGRES_PASSWORD:-admin}"

# SonarQube database configuration
SONAR_DB_HOST="${SONAR_DB_HOST:-localhost}"
SONAR_DB_PORT="${SONAR_DB_PORT:-5432}"
SONAR_DB_NAME="${SONAR_DB_NAME:-sonarqube}"
SONAR_DB_USER="${SONAR_DB_USER:-admin}"
SONAR_DB_PASSWORD="${SONAR_DB_PASSWORD:-admin}"

# Function to wait for PostgreSQL to be ready
wait_for_postgres() {
    local host=$1
    local port=$2
    local user=$3
    local password=$4
    local max_attempts=30
    local attempt=1
    
    print_info "Waiting for PostgreSQL at $host:$port to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        # Use Docker container to connect to PostgreSQL instead of local psql
        if docker exec postgres-sonar pg_isready -U "$user" -d sonarqube >/dev/null 2>&1; then
            print_success "PostgreSQL at $host:$port is ready!"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_error "PostgreSQL at $host:$port failed to become ready"
    return 1
}

# Function to create database if it doesn't exist
create_database_if_not_exists() {
    local host=$1
    local port=$2
    local admin_user=$3
    local admin_password=$4
    local db_name=$5
    local db_user=$6
    local db_password=$7
    
    print_info "Creating database '$db_name' if it doesn't exist..."
    
    # Check if database exists
    local db_exists=$(docker exec postgres-sonar psql -U "$admin_user" -d sonarqube -tAc "SELECT 1 FROM pg_database WHERE datname='$db_name'" 2>/dev/null)
    
    if [ "$db_exists" = "1" ]; then
        print_info "Database '$db_name' already exists"
    else
        # Create database
        docker exec postgres-sonar psql -U "$admin_user" -d sonarqube -c "CREATE DATABASE $db_name;" >/dev/null 2>&1
        if [ $? -eq 0 ]; then
            print_success "Database '$db_name' created successfully"
        else
            print_error "Failed to create database '$db_name'"
            return 1
        fi
    fi
    
    # Check if user exists
    local user_exists=$(docker exec postgres-sonar psql -U "$admin_user" -d sonarqube -tAc "SELECT 1 FROM pg_roles WHERE rolname='$db_user'" 2>/dev/null)
    
    if [ "$user_exists" = "1" ]; then
        print_info "User '$db_user' already exists"
    else
        # Create user
        docker exec postgres-sonar psql -U "$admin_user" -d sonarqube -c "CREATE USER $db_user WITH PASSWORD '$db_password';" >/dev/null 2>&1
        if [ $? -eq 0 ]; then
            print_success "User '$db_user' created successfully"
        else
            print_error "Failed to create user '$db_user'"
            return 1
        fi
    fi
    
    # Grant privileges
    docker exec postgres-sonar psql -U "$admin_user" -d sonarqube -c "GRANT ALL PRIVILEGES ON DATABASE $db_name TO $db_user;" >/dev/null 2>&1
    if [ $? -eq 0 ]; then
        print_success "Privileges granted to user '$db_user' on database '$db_name'"
    else
        print_warning "Failed to grant privileges (database may still work)"
    fi
    
    return 0
}

# Main execution
main() {
    print_info "Starting database initialization process..."
    
    # Wait for main PostgreSQL instance
    if ! wait_for_postgres "$DB_HOST" "$DB_PORT" "$DB_USER" "$DB_PASSWORD"; then
        print_error "Main PostgreSQL instance is not available"
        return 1
    fi
    
    # Create main application database
    if ! create_database_if_not_exists "$DB_HOST" "$DB_PORT" "$DB_USER" "$DB_PASSWORD" "$DB_NAME" "$DB_USER" "$DB_PASSWORD"; then
        print_error "Failed to setup main application database"
        return 1
    fi
    
    # Wait for SonarQube PostgreSQL instance (if different)
    if [ "$SONAR_DB_HOST" != "$DB_HOST" ] || [ "$SONAR_DB_PORT" != "$DB_PORT" ]; then
        if ! wait_for_postgres "$SONAR_DB_HOST" "$SONAR_DB_PORT" "$SONAR_DB_USER" "$SONAR_DB_PASSWORD"; then
            print_warning "SonarQube PostgreSQL instance is not available"
        else
            # Create SonarQube database
            if ! create_database_if_not_exists "$SONAR_DB_HOST" "$SONAR_DB_PORT" "$SONAR_DB_USER" "$SONAR_DB_PASSWORD" "$SONAR_DB_NAME" "$SONAR_DB_USER" "$SONAR_DB_PASSWORD"; then
                print_warning "Failed to setup SonarQube database"
            fi
        fi
    else
        # Same PostgreSQL instance, just create SonarQube database
        if ! create_database_if_not_exists "$DB_HOST" "$DB_PORT" "$DB_USER" "$DB_PASSWORD" "$SONAR_DB_NAME" "$DB_USER" "$DB_PASSWORD"; then
            print_warning "Failed to setup SonarQube database"
        fi
    fi
    
    print_success "Database initialization completed!"
    return 0
}

# Execute main function
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
    handle_exit $? "Database Initialization"
fi
