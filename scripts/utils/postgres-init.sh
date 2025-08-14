#!/bin/bash
set -e

# =============================================================================
# PostgreSQL Multiple Database Initialization Script
# =============================================================================
# This script creates multiple databases for the same PostgreSQL instance
# Used to support both SonarQube and Wallet API databases
# =============================================================================

function create_user_and_database() {
    local database=$1
    echo "Creating database '$database'"
    
    # Create database if it doesn't exist
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
        SELECT 'CREATE DATABASE $database'
        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$database')\gexec
EOSQL
    
    # Grant all privileges to the admin user on the new database
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$database" <<-EOSQL
        GRANT ALL PRIVILEGES ON DATABASE $database TO $POSTGRES_USER;
        GRANT ALL PRIVILEGES ON SCHEMA public TO $POSTGRES_USER;
        GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO $POSTGRES_USER;
        GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO $POSTGRES_USER;
        GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO $POSTGRES_USER;
EOSQL
    
    echo "Database '$database' created and configured successfully"
}

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
    echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
    for db in $(echo $POSTGRES_MULTIPLE_DATABASES | tr ',' ' '); do
        if [ "$db" != "$POSTGRES_DB" ]; then
            create_user_and_database $db
        else
            echo "Database '$db' is the default database, skipping creation"
        fi
    done
    echo "Multiple databases created successfully"
else
    echo "No multiple databases requested"
fi
