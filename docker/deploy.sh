#!/bin/bash
set -e

echo "=== Website Docker Deployment ==="

echo "Step 1: Fixing .env file format..."
sed -i '1s/^\xEF\xBB\xBF//' .env
sed -i 's/\r$//' .env

echo "Step 2: Stopping old containers..."
docker compose down

echo "Step 3: Starting MySQL first..."
docker compose up -d mysql

echo "Step 4: Waiting for MySQL to be ready..."
sleep 15

echo "Step 5: Fixing Flyway baseline (final)..."
docker exec -i website-mysql mysql -uroot -p"$(grep MYSQL_ROOT_PASSWORD .env | cut -d= -f2)" < fix-flyway-final.sql

echo "Step 6: Building backend (no cache)..."
docker compose build --no-cache backend

echo "Step 7: Starting all services..."
docker compose up -d

echo "Step 8: Waiting for backend to start..."
sleep 30

echo "Step 9: Checking container status..."
docker ps

echo "=== Deployment Complete ==="
echo "Check logs with: docker compose logs -f backend"
