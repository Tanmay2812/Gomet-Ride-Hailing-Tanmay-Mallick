# Scripts Directory

This directory contains utility scripts for the GoComet Ride Hailing application.

## Available Scripts

### `reset-demo.sh`
Resets the demo environment by clearing rides, trips, payments, and setting all drivers to AVAILABLE status.

**Usage:**
```bash
./scripts/reset-demo.sh
```

**What it does:**
1. Sets all drivers to AVAILABLE status
2. Updates driver locations in Redis (for testing)
3. Clears existing rides, trips, and payments

**Note:** Requires Docker containers to be running.

### `keep-drivers-online.sh`
Keeps drivers online by periodically updating their locations in Redis. Useful for testing and demos.

**Usage:**
```bash
./scripts/keep-drivers-online.sh
```

**What it does:**
1. Continuously updates driver locations every 10 seconds
2. Simulates active drivers for testing
3. Press Ctrl+C to stop

**Note:** Requires backend service to be running. Updates hardcoded driver IDs (1 and 2).

### `setup-newrelic.sh`
Interactive script to set up New Relic monitoring for the application.

**Usage:**
```bash
./scripts/setup-newrelic.sh
```

**What it does:**
1. Guides you through creating a New Relic account
2. Helps you get your license key
3. Updates `.env` file with license key
4. Optionally restarts backend service
5. Verifies New Relic connection

**Note:** Requires Docker containers to be running. See `docs/NEW_RELIC.md` for detailed documentation.

### `test-e2e.sh`
Runs comprehensive end-to-end tests for all APIs.

**Usage:**
```bash
./scripts/test-e2e.sh
```

**What it does:**
1. Checks if services are running
2. Resets environment
3. Creates a ride request
4. Tests driver matching
5. Tests ride acceptance
6. Tests trip lifecycle (start, end)
7. Tests payment processing
8. Provides summary of all tests

**Requirements:**
- Backend service running on port 8080
- PostgreSQL and Redis containers running
- Python 3 for JSON parsing

**Note:** This script performs a complete end-to-end test of the entire ride flow.

## Script Permissions

All scripts should be executable. If they're not, make them executable:

```bash
chmod +x scripts/*.sh
```

## Dependencies

- **Docker & Docker Compose** - Required for most scripts
- **curl** - Required for API testing
- **Python 3** - Required for `test-e2e.sh` JSON parsing

## Troubleshooting

### Scripts not executable
```bash
chmod +x scripts/*.sh
```

### Docker containers not running
```bash
docker-compose up -d
```

### Backend not accessible
```bash
# Check if backend is running
docker-compose ps backend

# Check backend logs
docker-compose logs backend
```

### Python not found
```bash
# Install Python 3
# macOS: brew install python3
# Linux: sudo apt-get install python3
```

## Best Practices

1. **Always run scripts from project root**
   ```bash
   ./scripts/reset-demo.sh
   ```

2. **Check service status before running scripts**
   ```bash
   docker-compose ps
   ```

3. **Review script output for errors**
   - Scripts provide colored output for success/failure
   - Check error messages if scripts fail

4. **Backup data before reset scripts**
   - `reset-demo.sh` clears all data
   - Backup database if needed

## Script Maintenance

- Scripts are tested and working
- Update scripts if API endpoints change
- Keep scripts in sync with application changes
- Document any new scripts added
