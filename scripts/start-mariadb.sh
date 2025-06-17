#!/bin/bash

# Start MariaDB for Task Management API Development
# This script starts the MariaDB Docker container and waits for it to be ready

echo "🐳 Starting MariaDB for Task Management API..."

# Start the MariaDB container
docker-compose up -d mariadb

echo "⏳ Waiting for MariaDB to be ready..."

# Wait for MariaDB to be healthy
timeout=60
counter=0
while [ $counter -lt $timeout ]; do
    if docker-compose exec mariadb mariadb -u taskuser -ptaskpassword -e "SELECT 1;" task_management >/dev/null 2>&1; then
        echo "✅ MariaDB is ready!"
        echo "📊 Database: task_management"
        echo "👤 User: taskuser"
        echo "🔗 Connection: localhost:3307"
        echo ""
        echo "💡 To connect with your application, use:"
        echo "   ./mvnw spring-boot:run -Dspring-boot.run.profiles=mariadb"
        echo ""
        echo "🌐 phpMyAdmin available at: http://localhost:8081"
        echo "   (Login: taskuser / taskpassword)"
        exit 0
    fi
    
    echo "⏳ Waiting... ($counter/$timeout seconds)"
    sleep 2
    counter=$((counter + 2))
done

echo "❌ MariaDB failed to start within $timeout seconds"
echo "📋 Check logs with: docker-compose logs mariadb"
exit 1 