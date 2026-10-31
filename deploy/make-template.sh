#!/bin/sh
# Saves the current demo database as smartcompany_template, the untouched
# copy deploy/reset-demo.sh restores every night. Run it once after the first
# start (when the dump has been restored), and again whenever the demo data
# should change, e.g. after restoring a new dump.
set -e
cd "$(dirname "$0")/.."

set -a
. ./.env
set +a

# nobody may be connected to a database while it's being copied
docker compose stop backend simulator

docker compose exec -T postgres dropdb -U smartcompany --if-exists smartcompany_template
docker compose exec -T postgres createdb -U smartcompany \
    --template=smartcompanydb --strategy=file_copy smartcompany_template

docker compose start backend simulator
echo "template saved at $(date -u)"
