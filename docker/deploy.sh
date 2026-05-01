#!/bin/bash
set -e

echo "=== Website Docker Deployment ==="
echo "Step 1: Fixing .env file format..."
# Remove BOM and convert CRLF to LF
sed -i '1s/^\xEF\xBB\xBF//' .env
sed -i 's/\r$//' .env

echo "Step 2: Fixing Flyway baseline..."
docker exec -i website-mysql mysql -uroot -p"$(grep MYSQL_ROOT_PASSWORD .env | cut -d= -f2)" < fix-flyway.sql

echo "Step 3: Stopping old containers..."
docker compose down

echo "Step 4: Building backend (no cache)..."
docker compose build --no-cache backend

echo "Step 5: Starting all services..."
docker compose up -d

echo "Step 6: Waiting for backend to start..."
sleep 30

echo "Step 7: Checking container status..."
docker ps

echo "=== Deployment Complete ==="
echo "Check logs with: docker compose logs -f backend"
