# Docker Setup & Deployment

## Overview

The GoComet Ride Hailing application is fully containerized using Docker and Docker Compose. This enables easy deployment, consistent environments, and simplified dependency management.

## Architecture

The application consists of 4 main services:

1. **PostgreSQL** - Database service
2. **Redis** - Caching and geospatial indexing
3. **Backend** - Spring Boot application
4. **Frontend** - React application served by Nginx

## Prerequisites

- **Docker** 20.10+ or **Docker Desktop**
- **Docker Compose** 2.0+
- **Colima** (for macOS without Docker Desktop)

### For macOS Users

If you don't have Docker Desktop, use Colima:

```bash
# Install Colima
brew install colima

# Start Colima
colima start

# Verify Docker is running
docker ps
```

## Docker Compose Configuration

### docker-compose.yml

The main orchestration file defines all services:

```yaml
services:
  postgres:
    image: postgres:15-alpine
    container_name: gocomet-postgres
    environment:
      POSTGRES_DB: ride_hailing
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - gocomet-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: gocomet-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - gocomet-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: gocomet-backend
    env_file:
      - .env
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/ride_hailing
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
      NEW_RELIC_LICENSE_KEY: ${NEW_RELIC_LICENSE_KEY:-}
      NEW_RELIC_ACCOUNT_ID: ${NEW_RELIC_ACCOUNT_ID:-}
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - gocomet-network
    restart: unless-stopped

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: gocomet-frontend
    environment:
      REACT_APP_API_URL: http://localhost:8080
      REACT_APP_WS_URL: http://localhost:8080/ws
    ports:
      - "3000:3000"
    depends_on:
      - backend
    networks:
      - gocomet-network
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:

networks:
  gocomet-network:
    driver: bridge
```

## Service Details

### 1. PostgreSQL Service

**Image**: `postgres:15-alpine`
- Lightweight Alpine-based image
- PostgreSQL 15 database

**Configuration**:
- Database: `ride_hailing`
- User: `postgres`
- Password: `postgres`
- Port: `5432`

**Volumes**:
- `postgres_data` - Persistent storage for database

**Health Check**:
- Checks if PostgreSQL is ready to accept connections
- Runs every 10 seconds

### 2. Redis Service

**Image**: `redis:7-alpine`
- Lightweight Alpine-based image
- Redis 7 in-memory data store

**Configuration**:
- Port: `6379`
- No password (development only)

**Volumes**:
- `redis_data` - Persistent storage for Redis data

**Health Check**:
- Pings Redis to verify it's running
- Runs every 10 seconds

### 3. Backend Service

**Build**: Custom Dockerfile (see below)
- Multi-stage build
- Includes New Relic agent

**Configuration**:
- Port: `8080`
- Environment variables from `.env` file
- Depends on PostgreSQL and Redis

**Dependencies**:
- Waits for PostgreSQL to be healthy
- Waits for Redis to be healthy

### 4. Frontend Service

**Build**: Custom Dockerfile in `frontend/` directory
- Multi-stage build
- Nginx for serving static files

**Configuration**:
- Port: `3000`
- Depends on backend service

## Backend Dockerfile

### Multi-Stage Build

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY backend ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Add New Relic agent
COPY newrelic/newrelic/newrelic.jar /app/newrelic.jar
COPY newrelic/newrelic/newrelic.yml /app/newrelic.yml

COPY --from=build /app/target/ride-hailing-1.0.0.jar app.jar

EXPOSE 8080

# With New Relic agent
ENTRYPOINT ["java", "-javaagent:/app/newrelic.jar", "-jar", "app.jar"]
```

**Stages**:
1. **Build Stage**: Compiles Java code using Maven
2. **Runtime Stage**: Runs the JAR file with New Relic agent

**Features**:
- Multi-stage build reduces final image size
- Includes New Relic Java agent
- Uses JRE (not JDK) for smaller image

## Frontend Dockerfile

Located in `frontend/Dockerfile`:

```dockerfile
# Build stage
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Runtime stage
FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]
```

**Stages**:
1. **Build Stage**: Builds React application
2. **Runtime Stage**: Serves static files with Nginx

## Environment Variables

### .env File

Create `.env` file in project root:

```bash
# New Relic Configuration
NEW_RELIC_LICENSE_KEY=your-license-key-here
NEW_RELIC_ACCOUNT_ID=your-account-id
```

**Important**: Add `.env` to `.gitignore` to avoid committing secrets.

## Usage

### Start All Services

```bash
docker-compose up -d
```

This will:
- Build images if needed
- Start all services
- Wait for health checks
- Run in detached mode (`-d`)

### Start Specific Service

```bash
docker-compose up -d postgres
docker-compose up -d redis
docker-compose up -d backend
docker-compose up -d frontend
```

### Stop All Services

```bash
docker-compose down
```

### Stop and Remove Volumes

```bash
docker-compose down -v
```

**Warning**: This will delete all database data!

### Rebuild Services

```bash
# Rebuild all services
docker-compose up -d --build

# Rebuild specific service
docker-compose up -d --build backend
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend

# Last 100 lines
docker-compose logs --tail=100 backend
```

### Check Service Status

```bash
docker-compose ps
```

### Execute Commands in Container

```bash
# Backend container
docker exec -it gocomet-backend bash

# PostgreSQL container
docker exec -it gocomet-postgres psql -U postgres -d ride_hailing

# Redis container
docker exec -it gocomet-redis redis-cli
```

## Network Configuration

### Default Network

All services are on the `gocomet-network` bridge network.

**Service Discovery**:
- Services can reach each other by service name
- Example: `postgres:5432`, `redis:6379`

### Port Mapping

- **PostgreSQL**: `localhost:5432` → `postgres:5432`
- **Redis**: `localhost:6379` → `redis:6379`
- **Backend**: `localhost:8080` → `backend:8080`
- **Frontend**: `localhost:3000` → `frontend:3000`

## Volumes

### Persistent Storage

- `postgres_data` - PostgreSQL database files
- `redis_data` - Redis persistence files

**Location**: Managed by Docker
- Linux: `/var/lib/docker/volumes/`
- macOS: Docker Desktop manages volumes

### Backup Database

```bash
# Backup
docker exec gocomet-postgres pg_dump -U postgres ride_hailing > backup.sql

# Restore
docker exec -i gocomet-postgres psql -U postgres ride_hailing < backup.sql
```

## Health Checks

### PostgreSQL Health Check

```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U postgres"]
  interval: 10s
  timeout: 5s
  retries: 5
```

### Redis Health Check

```yaml
healthcheck:
  test: ["CMD", "redis-cli", "ping"]
  interval: 10s
  timeout: 3s
  retries: 5
```

**Benefits**:
- Services wait for dependencies to be ready
- Automatic restart on failure
- Better startup ordering

## Troubleshooting

### Services Not Starting

1. **Check Logs**:
   ```bash
   docker-compose logs
   ```

2. **Check Port Conflicts**:
   ```bash
   # Check if ports are in use
   lsof -i :5432
   lsof -i :6379
   lsof -i :8080
   lsof -i :3000
   ```

3. **Check Docker Status**:
   ```bash
   docker ps
   docker-compose ps
   ```

### Database Connection Issues

1. **Verify PostgreSQL is Running**:
   ```bash
   docker-compose ps postgres
   ```

2. **Check Database Logs**:
   ```bash
   docker-compose logs postgres
   ```

3. **Test Connection**:
   ```bash
   docker exec -it gocomet-postgres psql -U postgres -d ride_hailing
   ```

### Redis Connection Issues

1. **Verify Redis is Running**:
   ```bash
   docker-compose ps redis
   ```

2. **Test Connection**:
   ```bash
   docker exec -it gocomet-redis redis-cli ping
   ```

### Backend Build Failures

1. **Check Build Logs**:
   ```bash
   docker-compose build backend
   ```

2. **Clear Maven Cache**:
   ```bash
   docker-compose build --no-cache backend
   ```

### Frontend Build Failures

1. **Check Build Logs**:
   ```bash
   docker-compose build frontend
   ```

2. **Clear npm Cache**:
   ```bash
   docker-compose build --no-cache frontend
   ```

## Production Considerations

### Security

1. **Change Default Passwords**:
   - Update PostgreSQL password
   - Use secrets management

2. **Network Security**:
   - Don't expose database ports publicly
   - Use reverse proxy for frontend
   - Enable SSL/TLS

3. **Environment Variables**:
   - Use secrets management
   - Don't commit `.env` files

### Performance

1. **Resource Limits**:
   ```yaml
   services:
     backend:
       deploy:
         resources:
           limits:
             cpus: '2'
             memory: 2G
   ```

2. **Scaling**:
   ```bash
   docker-compose up -d --scale backend=3
   ```

3. **Caching**:
   - Use Docker layer caching
   - Multi-stage builds for smaller images

### Monitoring

1. **Health Checks**: Already configured
2. **Logging**: Use centralized logging
3. **Metrics**: New Relic integration

## Best Practices

1. **Use .env Files**: Never commit secrets
2. **Health Checks**: Always configure health checks
3. **Multi-Stage Builds**: Reduce image sizes
4. **Volume Management**: Use named volumes
5. **Network Isolation**: Use custom networks
6. **Resource Limits**: Set appropriate limits
7. **Regular Updates**: Keep images updated

## Summary

The Docker setup provides:
- ✅ Easy deployment
- ✅ Consistent environments
- ✅ Service orchestration
- ✅ Health checks
- ✅ Persistent storage
- ✅ Network isolation
- ✅ Production-ready configuration

All services are configured to work together seamlessly with proper dependencies and health checks.
