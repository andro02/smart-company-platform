#!/bin/sh
# Puts the public demo back to its starting state: the database, an empty
# cache, and the uploaded files as they were (if data/files-original exists).
# Meant to run every night from root's cron, see the README.
#
# The database is copied from smartcompany_template, an untouched copy made
# once with deploy/make-template.sh. Copying a database is a file copy on
# disk, which takes a minute or two even for several GB, while restoring the
# dump inserts every row and rebuilds every index again (well over half an
# hour on a small server). Without the template it falls back to the dump.
set -e
cd "$(dirname "$0")/.."

# passwords and COMPOSE_FILE
set -a
. ./.env
set +a

started=$(date +%s)
docker compose stop backend simulator

has_template=$(docker compose exec -T postgres psql -U smartcompany -d postgres -tAc \
    "select 1 from pg_database where datname = 'smartcompany_template'")

docker compose exec -T postgres dropdb -U smartcompany --if-exists --force smartcompanydb
if [ "$has_template" = "1" ]; then
    docker compose exec -T postgres createdb -U smartcompany \
        --template=smartcompany_template --strategy=file_copy smartcompanydb
else
    echo "no template database, restoring the dump (slow); see deploy/make-template.sh"
    docker compose exec -T postgres createdb -U smartcompany smartcompanydb
    docker compose exec -T postgres sh /docker-entrypoint-initdb.d/restore-dump.sh
fi

docker compose exec -T redis redis-cli -a "$REDIS_PASSWORD" --no-auth-warning FLUSHALL

if [ -d data/files-original ]; then
    rm -rf data/files
    cp -a data/files-original data/files
fi

# on startup the backend creates the demo accounts again
docker compose start backend simulator
echo "demo reset at $(date -u), took $(( $(date +%s) - started )) s"
