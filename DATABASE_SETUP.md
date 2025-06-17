# Database Setup Guide

This guide explains how to test your Task Management API with both H2 (development) and MariaDB (production-like testing).

## 🎯 Quick Start

### Option 1: H2 Database (Quick Development)
```bash
# Start with H2 (in-memory database)
./scripts/run-with-h2.sh
```
- ✅ **Fast startup** - No external dependencies
- ✅ **Clean slate** - Fresh database every restart  
- ✅ **H2 Console** - Available at http://localhost:8080/h2-console
- ⚠️ **Limited** - Not 100% compatible with MariaDB

### Option 2: MariaDB (Production-like Testing)
```bash
# Start with MariaDB
./scripts/run-with-mariadb.sh
```
- ✅ **Production accuracy** - Same database as production
- ✅ **Data persistence** - Data survives restarts
- ✅ **phpMyAdmin** - Available at http://localhost:8081
- ⚠️ **Requires Docker** - Needs Docker and docker-compose

## 📋 Prerequisites

### For H2 Testing
- Java 17+
- Maven (or use `./mvnw`)

### For MariaDB Testing  
- Docker and Docker Compose
- Java 17+
- Maven (or use `./mvnw`)

## 🐳 MariaDB Docker Setup

### Database Configuration
- **Database**: `task_management`
- **User**: `taskuser`
- **Password**: `taskpassword`
- **Port**: `3307`
- **Root Password**: `rootpassword`

### Management Scripts

#### Start MariaDB
```bash
./scripts/start-mariadb.sh
```
This script:
- Starts MariaDB container
- Waits for database to be ready
- Shows connection information
- Starts phpMyAdmin (optional)

#### Stop MariaDB
```bash
./scripts/stop-mariadb.sh
```

#### Reset MariaDB Data
```bash
./scripts/reset-mariadb.sh
```
⚠️ **Warning**: This deletes ALL data!

## 🚀 Running the Application

### With H2 Database
```bash
# Using script (recommended)
./scripts/run-with-h2.sh

# Or manually
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Access Points:**
- API: http://localhost:8080/rest/v1/health
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:taskdb`
  - User: `sa`
  - Password: (empty)

### With MariaDB Database
```bash
# Using script (recommended) - auto-starts MariaDB if needed
./scripts/run-with-mariadb.sh

# Or manually (ensure MariaDB is running first)
./scripts/start-mariadb.sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=mariadb
```

**Access Points:**
- API: http://localhost:8080/rest/v1/health
- phpMyAdmin: http://localhost:8081
  - Server: `mariadb`
  - Username: `taskuser`
  - Password: `taskpassword`

## 🔧 Spring Profiles

### `dev` Profile (H2)
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:h2:mem:taskdb
  jpa:
    hibernate:
      ddl-auto: create-drop  # Fresh tables on restart
  h2:
    console:
      enabled: true
```

### `mariadb` Profile
```yaml
# application-mariadb.yml  
spring:
  datasource:
    url: jdbc:mariadb://localhost:3307/task_management
    username: taskuser
    password: taskpassword
  jpa:
    hibernate:
      ddl-auto: update  # Preserve data between restarts
```

## 🧪 Testing Strategy

### Development Workflow
1. **Quick iterations** → Use H2 (`./scripts/run-with-h2.sh`)
2. **Feature testing** → Switch to MariaDB (`./scripts/run-with-mariadb.sh`)
3. **Database compatibility** → Test both databases
4. **Production readiness** → Final testing with MariaDB

### Test Data Management

#### H2 (Development)
- Data is **lost** on application restart
- Perfect for clean testing
- Use for unit/integration tests

#### MariaDB (Production-like)
- Data **persists** between restarts
- Good for manual testing
- Reset with `./scripts/reset-mariadb.sh` when needed

## 📊 Database Monitoring

### H2 Console
- **URL**: http://localhost:8080/h2-console
- **Purpose**: Query data, inspect schema
- **Access**: Direct SQL queries

### phpMyAdmin (MariaDB)
- **URL**: http://localhost:8081  
- **Purpose**: Visual database management
- **Features**: Query builder, schema browser, data export

## 🐛 Troubleshooting

### MariaDB Won't Start
```bash
# Check if port 3307 is in use
lsof -i :3307

# View MariaDB logs
docker-compose logs mariadb

# Reset everything
./scripts/reset-mariadb.sh
./scripts/start-mariadb.sh
```

### Connection Issues
```bash
# Test MariaDB connection
docker-compose exec mariadb mysql -u taskuser -ptaskpassword task_management

# Check container status
docker-compose ps
```

### JPA Schema Issues
```bash
# View generated schema in logs
./mvnw spring-boot:run -Dspring-boot.run.profiles=mariadb

# Look for lines like:
# Hibernate: create table tasks (...)
```

## 🔄 Switching Between Databases

### During Development
```bash
# Stop current application (Ctrl+C)

# Switch to H2
./scripts/run-with-h2.sh

# Or switch to MariaDB  
./scripts/run-with-mariadb.sh
```

### For Testing
```bash
# Test endpoint with H2
./scripts/run-with-h2.sh
curl -X POST http://localhost:8080/rest/v1/tasks \
  -F "file=@test.xlsx" \
  -H "Authorization: Bearer dummy-token"

# Stop and test same endpoint with MariaDB
./scripts/run-with-mariadb.sh
curl -X POST http://localhost:8080/rest/v1/tasks \
  -F "file=@test.xlsx" \  
  -H "Authorization: Bearer dummy-token"
```

## 📈 Performance Comparison

| Aspect | H2 | MariaDB |
|--------|----|---------| 
| **Startup Time** | ~3 seconds | ~8 seconds |
| **Memory Usage** | Lower | Higher |
| **Data Persistence** | No | Yes |
| **Production Accuracy** | ~85% | 100% |
| **Setup Complexity** | None | Docker required |

## 💡 Recommendations

### For Daily Development
- Use **H2** for rapid development cycles
- Use **MariaDB** before committing major changes
- Reset MariaDB weekly to prevent data accumulation

### For CI/CD
- Use **H2** for unit tests (fast)
- Use **MariaDB** for integration tests (accurate)
- Consider TestContainers for automated testing

### For Production Deployment
- Use **MariaDB/MySQL** production database
- Use `ddl-auto: validate` (not `update`)
- Enable connection pooling optimization
- Monitor performance with real production data 