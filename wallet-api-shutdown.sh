#!/bin/bash

# =============================================================================
# RecargaPay Wallet API - Complete Shutdown Script
# =============================================================================
# This script gracefully stops all services while preserving data:
# 1. Stops Spring Boot application
# 2. Stops all Docker Compose services
# 3. Preserves all volumes and data
# 4. Provides status feedback and cleanup information
# =============================================================================

# Set color variables for better output formatting
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Configuration
API_PORT=8080
SPRING_BOOT_PROCESS_NAME="spring-boot:run"
APPLICATION_LOG="application.log"

# =============================================================================
# UTILITY FUNCTIONS
# =============================================================================

print_header() {
    echo -e "\n${BOLD}${CYAN}🛑 RecargaPay Wallet API - Complete Shutdown${NC}"
    echo -e "${CYAN}===============================================${NC}\n"
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

# Function to check if a process is running
is_process_running() {
    local process_name=$1
    pgrep -f "$process_name" >/dev/null 2>&1
}

# Function to check if a port is in use
is_port_in_use() {
    local port=$1
    nc -z localhost $port >/dev/null 2>&1
}

# Function to check if Docker Compose services are running
are_docker_services_running() {
    local running_containers=$(docker-compose ps -q 2>/dev/null | wc -l)
    [ "$running_containers" -gt 0 ]
}

# Function to gracefully stop a process
stop_process_gracefully() {
    local process_name=$1
    local max_wait=30
    local wait_count=0
    
    if is_process_running "$process_name"; then
        print_info "Stopping $process_name..."
        
        # Send SIGTERM first (graceful shutdown)
        pkill -f "$process_name" 2>/dev/null
        
        # Wait for graceful shutdown
        while is_process_running "$process_name" && [ $wait_count -lt $max_wait ]; do
            echo -n "."
            sleep 1
            wait_count=$((wait_count + 1))
        done
        
        # If still running, force kill
        if is_process_running "$process_name"; then
            print_warning "Process didn't stop gracefully, forcing shutdown..."
            pkill -9 -f "$process_name" 2>/dev/null
            sleep 2
        fi
        
        if ! is_process_running "$process_name"; then
            print_success "$process_name stopped successfully"
            return 0
        else
            print_error "Failed to stop $process_name"
            return 1
        fi
    else
        print_info "$process_name is not running"
        return 0
    fi
}

# =============================================================================
# SERVICE STATUS CHECK
# =============================================================================

check_services_status() {
    print_step "Checking Services Status"
    
    local services_running=false
    
    # Check wallet-api Docker container
    if docker ps --format "table {{.Names}}" | grep -q "^wallet-api$"; then
        print_info "wallet-api Docker container is running"
        services_running=true
        
        # Show container details
        local container_info=$(docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep "^wallet-api")
        if [ -n "$container_info" ]; then
            echo -e "  ${CYAN}• $container_info${NC}"
        fi
    elif is_port_in_use $API_PORT; then
        print_info "Spring Boot application is running on port $API_PORT (possibly Maven process)"
        services_running=true
    else
        print_info "wallet-api application is not running"
    fi
    
    # Check Docker Compose services
    if are_docker_services_running; then
        print_info "Docker Compose services are running"
        services_running=true
        
        # List running containers
        print_info "Currently running containers:"
        docker-compose ps --format "table {{.Name}}\t{{.State}}\t{{.Ports}}" 2>/dev/null | grep -v "^NAME" | while read line; do
            if [ -n "$line" ]; then
                echo -e "  ${CYAN}• $line${NC}"
            fi
        done
    else
        print_info "No Docker Compose services are running"
    fi
    
    if [ "$services_running" = false ]; then
        print_success "No services are currently running"
        echo -e "\n${BOLD}${GREEN}🎯 All services are already stopped!${NC}\n"
        exit 0
    fi
}

# =============================================================================
# SPRING BOOT SHUTDOWN
# =============================================================================

stop_spring_boot() {
    print_step "Stopping Spring Boot Application (Docker Container)"
    
    # Check if wallet-api container is running
    if docker ps --format "table {{.Names}}" | grep -q "^wallet-api$"; then
        print_info "Found wallet-api container running, stopping it..."
        
        # Stop the container gracefully
        if docker stop wallet-api >/dev/null 2>&1; then
            print_success "wallet-api container stopped successfully"
            
            # Remove the container
            if docker rm wallet-api >/dev/null 2>&1; then
                print_success "wallet-api container removed successfully"
            else
                print_warning "Could not remove wallet-api container (may need manual cleanup)"
            fi
        else
            print_error "Failed to stop wallet-api container"
            print_info "You may need to stop it manually: docker stop wallet-api"
        fi
        
        # Verify port is no longer in use
        sleep 2
        if ! is_port_in_use $API_PORT; then
            print_success "Port $API_PORT is now free"
        else
            print_warning "Port $API_PORT may still be in use by another process"
        fi
    else
        # Check if there's a Maven process running (fallback for development mode)
        if stop_process_gracefully "$SPRING_BOOT_PROCESS_NAME"; then
            print_success "Maven Spring Boot process stopped"
            
            # Verify port is no longer in use
            sleep 2
            if ! is_port_in_use $API_PORT; then
                print_success "Spring Boot application stopped and port $API_PORT is now free"
            else
                print_warning "Port $API_PORT may still be in use by another process"
            fi
        else
            print_info "No wallet-api container or Maven process found running"
        fi
    fi
    
    # Archive application log if it exists
    if [ -f "$APPLICATION_LOG" ]; then
        local timestamp=$(date +"%Y%m%d_%H%M%S")
        local archived_log="logs/application_${timestamp}.log"
        
        # Create logs directory if it doesn't exist
        mkdir -p logs
        
        # Move log file to archive
        mv "$APPLICATION_LOG" "$archived_log" 2>/dev/null
        if [ $? -eq 0 ]; then
            print_success "Application log archived to: $archived_log"
        else
            print_info "Could not archive application log"
        fi
    fi
    
    # Archive wallet-api JSON logs if they exist
    if ls logs/wallet-api*.json >/dev/null 2>&1; then
        local timestamp=$(date +"%Y%m%d_%H%M%S")
        local archived_dir="logs/archived_${timestamp}"
        
        mkdir -p "$archived_dir"
        mv logs/wallet-api*.json "$archived_dir/" 2>/dev/null
        if [ $? -eq 0 ]; then
            print_success "Wallet API JSON logs archived to: $archived_dir"
        else
            print_info "Could not archive JSON logs"
        fi
    fi
}

# =============================================================================
# DOCKER SERVICES SHUTDOWN
# =============================================================================

stop_docker_services() {
    print_step "Stopping Docker Compose Services"
    
    if are_docker_services_running; then
        print_info "Stopping all Docker Compose services..."
        
        # Use docker-compose stop for graceful shutdown (preserves data)
        if docker-compose stop; then
            print_success "All Docker Compose services stopped successfully"
            
            # Verify services are stopped
            sleep 2
            if ! are_docker_services_running; then
                print_success "Confirmed: All Docker services are stopped"
            else
                print_warning "Some Docker services may still be running"
            fi
        else
            print_error "Failed to stop some Docker Compose services"
            print_info "You may need to run 'docker-compose down' manually"
        fi
    else
        print_info "No Docker Compose services are running"
    fi
}

# =============================================================================
# CLEANUP AND STATUS
# =============================================================================

cleanup_and_status() {
    print_step "Cleanup and Final Status"
    
    # Clean up any orphaned processes
    print_info "Checking for orphaned processes..."
    
    # Look for any remaining Java processes that might be related
    local java_processes=$(pgrep -f "java.*wallet" 2>/dev/null | wc -l)
    if [ "$java_processes" -gt 0 ]; then
        print_warning "Found $java_processes Java processes that may be related to the wallet API"
        print_info "You may want to check: ps aux | grep java"
    else
        print_success "No orphaned Java processes found"
    fi
    
    # Check final port status
    if ! is_port_in_use $API_PORT; then
        print_success "Port $API_PORT is now available"
    else
        print_warning "Port $API_PORT is still in use by another process"
        print_info "Check what's using the port: lsof -i :$API_PORT"
    fi
    
    # Docker status
    local running_containers=$(docker ps -q | wc -l)
    if [ "$running_containers" -eq 0 ]; then
        print_success "No Docker containers are running"
    else
        print_info "$running_containers Docker containers are still running (may be from other projects)"
    fi
}

# =============================================================================
# FINAL INFORMATION
# =============================================================================

display_final_info() {
    print_step "🎯 Shutdown Complete - Status Information"
    
    echo -e "\n${BOLD}${GREEN}🛑 All RecargaPay Wallet API services have been stopped!${NC}\n"
    
    echo -e "${BOLD}${CYAN}📊 What was stopped:${NC}"
    echo -e "${YELLOW}┌─────────────────────────────────────────────────────────────┐${NC}"
    echo -e "${YELLOW}│${NC} ${BOLD}Application Services${NC}                                    ${YELLOW}│${NC}"
    echo -e "${YELLOW}├─────────────────────────────────────────────────────────────┤${NC}"
    echo -e "${YELLOW}│${NC} 🌱 Spring Boot Application (port 8080)                    ${YELLOW}│${NC}"
    echo -e "${YELLOW}│${NC} 📝 Application logs archived                               ${YELLOW}│${NC}"
    echo -e "${YELLOW}├─────────────────────────────────────────────────────────────┤${NC}"
    echo -e "${YELLOW}│${NC} ${BOLD}Infrastructure Services${NC}                               ${YELLOW}│${NC}"
    echo -e "${YELLOW}├─────────────────────────────────────────────────────────────┤${NC}"
    echo -e "${YELLOW}│${NC} 🐘 PostgreSQL (data preserved)                            ${YELLOW}│${NC}"
    echo -e "${YELLOW}│${NC} 🔴 Redis (data preserved)                                 ${YELLOW}│${NC}"
    echo -e "${YELLOW}│${NC} 📈 Prometheus (data preserved)                            ${YELLOW}│${NC}"
    echo -e "${YELLOW}│${NC} 📊 Grafana (dashboards preserved)                         ${YELLOW}│${NC}"
    echo -e "${YELLOW}│${NC} 🔍 Loki (logs preserved)                                  ${YELLOW}│${NC}"
    echo -e "${YELLOW}│${NC} 🔧 SonarQube (analysis data preserved)                    ${YELLOW}│${NC}"
    echo -e "${YELLOW}└─────────────────────────────────────────────────────────────┘${NC}"
    
    echo -e "\n${BOLD}${CYAN}💾 Data Preservation:${NC}"
    echo -e "${GREEN}✅ All Docker volumes preserved${NC}"
    echo -e "${GREEN}✅ Database data intact${NC}"
    echo -e "${GREEN}✅ Redis cache data intact${NC}"
    echo -e "${GREEN}✅ Grafana dashboards preserved${NC}"
    echo -e "${GREEN}✅ Prometheus metrics history preserved${NC}"
    echo -e "${GREEN}✅ SonarQube analysis history preserved${NC}"
    
    echo -e "\n${BOLD}${CYAN}🚀 To restart services:${NC}"
    echo -e "${BLUE}./wallet-api-startup.sh${NC}"
    
    echo -e "\n${BOLD}${CYAN}📝 Useful Commands:${NC}"
    echo -e "${GREEN}• Check Docker status:${NC} docker ps -a"
    echo -e "${GREEN}• Check volumes:${NC} docker volume ls"
    echo -e "${GREEN}• Check port usage:${NC} lsof -i :8080"
    echo -e "${GREEN}• View archived logs:${NC} ls -la logs/"
    
    echo -e "\n${BOLD}${CYAN}🗑️ If you want to remove data (DESTRUCTIVE):${NC}"
    echo -e "${RED}• Remove containers & volumes:${NC} docker-compose down -v"
    echo -e "${RED}• Remove all Docker data:${NC} docker system prune -a --volumes"
    
    echo -e "\n${BOLD}${GREEN}😴 Services are now sleeping. Sweet dreams! 💤${NC}\n"
}

# =============================================================================
# MAIN EXECUTION
# =============================================================================

main() {
    print_header
    
    # Execute all steps
    check_services_status
    stop_spring_boot
    stop_docker_services
    cleanup_and_status
    display_final_info
    
    print_success "Shutdown script completed successfully!"
}

# Trap to handle script interruption
trap 'echo -e "\n${RED}Shutdown script interrupted. Some services may still be running.${NC}"; exit 1' INT TERM

# Run main function
main "$@"
