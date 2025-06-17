#!/bin/bash

# Run Task Management API with MariaDB Database

echo "🚀 Starting Task Management API with MariaDB..."

# Check if MariaDB is running
if ! docker-compose ps mariadb | grep -q "Up"; then
    echo "⚠️  MariaDB is not running. Starting it first..."
    ./scripts/start-mariadb.sh
    if [ $? -ne 0 ]; then
        echo "❌ Failed to start MariaDB. Exiting."
        exit 1
    fi
fi

echo "📊 Database: MariaDB (localhost:3307)"
echo "🌐 phpMyAdmin: http://localhost:8081"
echo "🔗 API Health: http://localhost:8080/rest/v1/health"
echo ""

./mvnw spring-boot:run -Dspring-boot.run.profiles=mariadb 