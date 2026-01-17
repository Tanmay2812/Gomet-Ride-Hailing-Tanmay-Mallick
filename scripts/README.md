# Scripts Directory

This directory contains utility scripts for the GoComet Ride Hailing application.

## Available Scripts

### `sync-to-main.sh`
Syncs all code changes from `dev` branch to `main` branch, excluding the `docs/` folder.

**Usage:**
```bash
./scripts/sync-to-main.sh
```

**What it does:**
1. Ensures you're on `dev` branch
2. Pulls latest changes
3. Switches to `main` branch
4. Merges `dev` into `main`
5. Removes `docs/` folder from main
6. Commits the changes
7. Switches back to `dev` branch

**Note:** This script preserves `README.md` in main branch and only excludes the `docs/` folder.

### `reset-demo.sh`
Resets the demo environment by clearing rides, trips, payments, and setting all drivers to AVAILABLE status.

**Usage:**
```bash
./scripts/reset-demo.sh
```

### `keep-drivers-online.sh`
Keeps drivers online by periodically updating their locations in Redis.

**Usage:**
```bash
./scripts/keep-drivers-online.sh
```

### `test-e2e.sh`
Runs end-to-end tests for all APIs.

**Usage:**
```bash
./scripts/test-e2e.sh
```
