#!/bin/sh
# Fills the database from the dump in data/. Runs by itself the first time the
# database volume is created, and again from reset-demo.sh every night.
set -e

DUMP=/dump/smartcompanydb.dump
if [ ! -f "$DUMP" ]; then
    echo "no dump in data/, starting with an empty database"
    exit 0
fi

# pg_dump's custom format starts with "PGDMP"; otherwise it's plain SQL
if [ "$(head -c 5 "$DUMP")" = "PGDMP" ]; then
    pg_restore --no-owner --no-privileges -U "$POSTGRES_USER" -d "$POSTGRES_DB" "$DUMP"
else
    psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f "$DUMP"
fi
