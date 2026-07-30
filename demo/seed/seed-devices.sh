#!/usr/bin/env bash
# seed-devices.sh — Seeds Mini-NMS with sample devices via REST API

API_URL="${API_URL:-http://localhost:8080/api/devices}"

echo "============================================================"
echo "          Mini-NMS Device Seeding Script"
echo "============================================================"
echo "Targeting API Endpoint: ${API_URL}"
echo ""

devices=(
  '{"name": "Google Primary DNS", "ipAddress": "8.8.8.8"}'
  '{"name": "Google Secondary DNS", "ipAddress": "8.8.4.4"}'
  '{"name": "Cloudflare Primary DNS", "ipAddress": "1.1.1.1"}'
  '{"name": "Cloudflare Secondary DNS", "ipAddress": "1.0.0.1"}'
  '{"name": "Quad9 Security DNS", "ipAddress": "9.9.9.9"}'
  '{"name": "OpenDNS Primary Resolver", "ipAddress": "208.67.222.222"}'
  '{"name": "Localhost Gateway", "ipAddress": "127.0.0.1"}'
  '{"name": "Unreachable Node Alpha", "ipAddress": "192.0.2.1"}'
  '{"name": "Unreachable Node Beta", "ipAddress": "198.51.100.1"}'
)

count=0
success=0
failed=0

for payload in "${devices[@]}"; do
  count=$((count + 1))
  name=$(echo "$payload" | grep -o '"name": "[^"]*' | cut -d'"' -f4)
  ip=$(echo "$payload" | grep -o '"ipAddress": "[^"]*' | cut -d'"' -f4)

  printf "[%d/%d] Registering '%s' (%s)... " "$count" "${#devices[@]}" "$name" "$ip"

  response=$(curl -s -w "\n%{http_code}" -X POST "${API_URL}" \
    -H "Content-Type: application/json" \
    -d "$payload")

  http_code=$(echo "$response" | tail -n1)
  body=$(echo "$response" | sed '$d')

  if [ "$http_code" -eq 201 ]; then
    echo "SUCCESS (201 Created)"
    success=$((success + 1))
  elif [ "$http_code" -eq 409 ]; then
    echo "SKIPPED (409 Already Registered)"
    success=$((success + 1))
  else
    echo "FAILED (HTTP ${http_code}): ${body}"
    failed=$((failed + 1))
  fi
done

echo ""
echo "============================================================"
echo "Seeding completed: ${success} registered/existing, ${failed} failed."
echo "============================================================"
