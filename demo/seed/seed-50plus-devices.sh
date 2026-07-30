#!/usr/bin/env bash
# seed-50plus-devices.sh — Seeds Mini-NMS with 60+ devices via REST API curl calls

API_URL="${API_URL:-http://localhost:8080/api/devices}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SEED_FILE="${SCRIPT_DIR}/seed_50plus_devices.json"

echo "============================================================"
echo "          Mini-NMS 60-Device Bulk Seeding Script"
echo "============================================================"
echo "Targeting API Endpoint: ${API_URL}"
echo ""

if [ ! -f "$SEED_FILE" ]; then
  echo "Error: Seed file not found at ${SEED_FILE}"
  exit 1
fi

python3 "${SCRIPT_DIR}/seed_devices.py"
