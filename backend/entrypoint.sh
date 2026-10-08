#!/bin/sh
set -e

# Render provides DATABASE_URL in the form:
#   postgres://user:password@host:port/dbname
# Spring Boot needs: jdbc:postgresql://user:password@host:port/dbname
# This script converts it automatically (unless SPRING_DATASOURCE_URL is set).
if [ -n "$DATABASE_URL" ] && [ -z "$SPRING_DATASOURCE_URL" ]; then
  DB_NO_SCHEME="${DATABASE_URL#postgres://}"
  CREDENTIALS="${DB_NO_SCHEME%%@*}"
  HOST_PART="${DB_NO_SCHEME#*@}"

  export SPRING_DATASOURCE_URL="jdbc:postgresql://${HOST_PART}?sslmode=require"
  export SPRING_DATASOURCE_USERNAME="${CREDENTIALS%%:*}"
  export SPRING_DATASOURCE_PASSWORD="${CREDENTIALS#*:}"
fi

echo "Starting Farmer backend on port ${PORT:-8080}..."
exec java -XX:MaxRAMPercentage=75 -jar /app/app.jar --server.port="${PORT:-8080}"
