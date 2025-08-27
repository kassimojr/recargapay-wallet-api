#!/bin/bash

# Monitoring Setup Script
# This script manages PostgreSQL, Prometheus, and Grafana services using Docker Compose

# Load common utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/utils/common.sh"

# Ensure we're in the project root
ensure_project_root

print_step "Monitoring Environment Setup"

# Set color variables for better output formatting
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}Digital Wallet API Monitoring Environment${NC}"
echo -e "${YELLOW}This script manages PostgreSQL, Prometheus, and Grafana services using Docker Compose${NC}"

# Function to check if a container is running
is_container_running() {
  local container_name=$1
  if [ "$(docker ps -q -f name=$container_name)" ]; then
    return 0 # Container is running
  else
    return 1 # Container is not running
  fi
}

# Check if Wallet API is running on port 8080
check_wallet_api() {
  # Try to connect to port 8080, timeout after 1 second
  nc -z -w1 localhost 8080 &>/dev/null
  if [ $? -eq 0 ]; then
    echo -e "\n${GREEN}Wallet API is already running on port 8080${NC}"
    echo -e "${YELLOW}Make sure it's configured to connect to the monitoring stack${NC}"
    return 0 # API is running
  else
    return 1 # API is not running
  fi
}

# Check if key monitoring services are already running
if is_container_running "wallet-prometheus" && is_container_running "wallet-grafana" && is_container_running "postgres-sonar"; then
  echo -e "\n${BLUE}Monitoring services are already running!${NC}"
  echo -e "${YELLOW}If you want to restart them, run: 'docker-compose down' first.${NC}"
else
  # Start the services
  echo -e "\n${BLUE}Starting monitoring services...${NC}"
  docker-compose up -d

  # Check if services started successfully
  echo -e "\n${BLUE}Waiting for services to start...${NC}"
  sleep 5
  
  if ! is_container_running "wallet-prometheus" || ! is_container_running "wallet-grafana" || ! is_container_running "postgres-sonar"; then
    echo -e "${RED}Warning: Not all services started correctly. Please check docker logs.${NC}"
  else
    echo -e "${GREEN}All monitoring services started successfully!${NC}"
  fi
fi

# Check if the Wallet API is running
check_wallet_api
api_running=$?

# Display information about services
echo -e "\n${GREEN}Monitoring Environment Information:${NC}"
echo -e "${YELLOW}Available services:${NC}"
echo -e "- PostgreSQL: localhost:5432 (used by the Wallet API)"
echo -e "- Prometheus: http://localhost:9090 (metrics collection)"
echo -e "- Grafana: http://localhost:3000 (dashboards, default login: admin/admin)"
echo -e "- SonarQube: http://localhost:9000 (code quality analysis, default login: admin/admin)"

echo -e "\n${YELLOW}To view service logs:${NC}"
echo -e "- Prometheus: docker logs -f wallet-prometheus"
echo -e "- Grafana: docker logs -f wallet-grafana"
echo -e "- PostgreSQL: docker logs -f postgres-sonar"

echo -e "\n${YELLOW}To stop the environment:${NC}"
echo -e "- docker-compose down"

# Only display API endpoints if the API is not running
if [ $api_running -ne 0 ]; then
  echo -e "\n${YELLOW}Wallet API is not running. To start it:${NC}"
  echo -e "${BLUE}./mvnw spring-boot:run${NC}"
  
  echo -e "\n${YELLOW}When the API is running, these endpoints will be available:${NC}"
  echo -e "- API: http://localhost:8080/swagger-ui/index.html"
  echo -e "- Health Check: http://localhost:8080/actuator/health"
  echo -e "- Prometheus Metrics: http://localhost:8080/actuator/prometheus"
else
  echo -e "\n${YELLOW}Wallet API endpoints:${NC}"
  echo -e "- API: http://localhost:8080/swagger-ui/index.html"
  echo -e "- Health Check: http://localhost:8080/actuator/health"
  echo -e "- Prometheus Metrics: http://localhost:8080/actuator/prometheus"
  
  echo -e "\n${GREEN}===== MONITORING TEST GUIDE =====${NC}"
  echo -e "${YELLOW}1. Check http://localhost:8080/actuator/health for health checks${NC}"
  echo -e "${YELLOW}2. View http://localhost:8080/actuator/prometheus for raw metrics${NC}"
  echo -e "${YELLOW}3. Verify http://localhost:9090/targets to confirm Prometheus is collecting data${NC}"
  echo -e "${YELLOW}4. Access http://localhost:3000 and navigate to Digital folder > Wallet API Monitoring dashboard${NC}"
  
  echo -e "\n${GREEN}===== API AUTHENTICATION =====${NC}"
  echo -e "${YELLOW}Before calling any endpoints, you need to authenticate:${NC}"
  echo -e "\n${BLUE}# 1. Login to get a token${NC}"
  echo -e "TOKEN=\$(curl -s -X POST http://localhost:8080/api/auth/login -H \"Content-Type: application/json\" -d '{\"username\":\"admin\",\"password\":\"password\"}' | grep -o '\"token\":\"[^\"]*\"' | cut -d'\"' -f4)"
  echo -e "\n${BLUE}# 2. Use the token in subsequent requests${NC}"
  echo -e "echo \"Your token: \$TOKEN\""
  
  echo -e "\n${GREEN}===== API TEST COMMANDS =====${NC}"
  echo -e "${YELLOW}Use these commands with your auth token:${NC}"
  echo -e "\n${BLUE}# Create a wallet${NC}"
  echo -e "curl -X POST http://localhost:8080/api/v1/wallet -H \"Content-Type: application/json\" -H \"Authorization: Bearer \$TOKEN\" -d '{\"userId\":\"user123\",\"initialBalance\":100.00}'"
  echo -e "\n${BLUE}# Make a deposit${NC}"
  echo -e "curl -X POST http://localhost:8080/api/v1/wallet/{WALLET_ID}/deposit -H \"Content-Type: application/json\" -H \"Authorization: Bearer \$TOKEN\" -d '{\"amount\":50.00}'"
  echo -e "\n${BLUE}# Make a withdrawal${NC}"
  echo -e "curl -X POST http://localhost:8080/api/v1/wallet/{WALLET_ID}/withdraw -H \"Content-Type: application/json\" -H \"Authorization: Bearer \$TOKEN\" -d '{\"amount\":25.00}'"
fi
