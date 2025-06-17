#!/bin/bash

# Stop MariaDB for Task Management API Development

echo "🛑 Stopping MariaDB..."

# Stop the MariaDB container
docker-compose stop mariadb phpmyadmin

echo "✅ MariaDB stopped successfully!"
echo ""
echo "💡 To restart MariaDB, use:"
echo "   ./scripts/start-mariadb.sh"
echo ""
echo "🗑️  To completely remove MariaDB data, use:"
echo "   ./scripts/reset-mariadb.sh" 