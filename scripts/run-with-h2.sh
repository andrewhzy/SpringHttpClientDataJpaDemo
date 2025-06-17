#!/bin/bash

# Run Task Management API with H2 Database (Development Mode)

echo "🚀 Starting Task Management API with H2 Database..."
echo "📊 Database: In-memory H2"
echo "🌐 H2 Console: http://localhost:8080/h2-console"
echo "🔗 API Health: http://localhost:8080/rest/v1/health"
echo ""

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev 