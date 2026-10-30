#!/bin/sh
# Puts the public demo back to its starting state: the database from the dump,
# an empty cache, and the uploaded files as they were (if data/files-original
# exists). Meant to run every night from cron, see the README.
set -e
cd "$(dirname "$0")/.."

# passwords and COMPOSE_FILE
set -a
. ./.env
set +a

docker compose stop backend simulator

docker compose exec -T postgres dropdb -U smartcompany --if-exists --force smartcompanydb
docker compose exec -T postgres createdb -U smartcompany smartcompanydb
docker compose exec -T postgres sh /docker-entrypoint-initdb.d/restore-dump.sh

docker compose exec -T redis redis-cli -a "$REDIS_PASSWORD" --no-auth-warning FLUSHALL

if [ -d data/files-original ]; then
    rm -rf data/files
    cp -a data/files-original data/files
fi

# on startup the backend creates the demo accounts again
docker compose start backend simulator
echo "demo reset at $(date -u)"
