#!/bin/sh
# wait-for-statistics.sh
set -e

host="$1"
port="$2"
shift 2

# Check if host and port are provided
if [ -z "$host" ] || [ -z "$port" ]; then
  echo "Usage: wait-for-statistics.sh host port -- command args"
  exit 1
fi

# Wait for database to be ready using /dev/tcp
echo "waiting for statistics database at $host:$port..."
for i in $(seq 1 30); do
  if echo > /dev/tcp/$host/$port 2>/dev/null; then
    echo "statistics database is ready!"
    break
  fi
  if [ $i -eq 30 ]; then
    echo "timeout waiting for statistics database at $host:$port"
    exit 1
  fi
  sleep 2
done

exec "$@"
