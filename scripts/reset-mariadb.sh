#!/bin/bash

# Reset MariaDB data for Task Management API Development
# WARNING: This will delete all data in the MariaDB container

echo "⚠️  WARNING: This will delete ALL MariaDB data!"
echo "This action cannot be undone."
echo ""
read -p "Are you sure you want to continue? (yes/no): " -r
echo ""

if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
    echo "❌ Operation cancelled."
    exit 1
fi

echo "🗑️  Stopping and removing MariaDB containers..."
docker-compose down mariadb phpmyadmin

echo "🗑️  Removing MariaDB data volume..."
docker volume rm springhttpclientdatajpademo_mariadb_data 2>/dev/null || true

echo "✅ MariaDB data reset successfully!"
echo ""
echo "💡 To start fresh MariaDB, use:"
echo "   ./scripts/start-mariadb.sh" 