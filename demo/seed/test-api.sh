#!/usr/bin/env bash
# test-api.sh — Automated REST API Verification Script for Mini-NMS

BASE_URL="${BASE_URL:-http://localhost:8080}"
GREEN='\031[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "============================================================"
echo "          Mini-NMS REST API Verification Test"
echo "============================================================"
echo "Base URL: ${BASE_URL}"
echo ""

PASSED=0
FAILED=0

assert_status() {
  local test_name="$1"
  local expected_status="$2"
  local actual_status="$3"

  if [ "$actual_status" -eq "$expected_status" ]; then
    echo -e "[PASS] ${test_name} (HTTP ${actual_status})"
    PASSED=$((PASSED + 1))
  else
    echo -e "[FAIL] ${test_name} (Expected HTTP ${expected_status}, got ${actual_status})"
    FAILED=$((FAILED + 1))
  fi
}

# 1. Test GET /api/devices
echo "--- 1. Testing Device Listing ---"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/devices")
assert_status "GET /api/devices" 200 "$HTTP_CODE"

# 2. Test POST /api/devices (Valid)
echo ""
echo "--- 2. Testing Device Registration ---"
RAND_IP="10.200.$((RANDOM % 250 + 1)).$((RANDOM % 250 + 1))"
RESP=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/api/devices" \
  -H "Content-Type: application/json" \
  -d "{\"name\": \"Test Node ${RAND_IP}\", \"ipAddress\": \"${RAND_IP}\"}")

HTTP_CODE=$(echo "$RESP" | tail -n1)
BODY=$(echo "$RESP" | sed '$d')
assert_status "POST /api/devices (Valid IPv4)" 201 "$HTTP_CODE"

DEVICE_ID=$(echo "$BODY" | grep -o '"id":"[^"]*' | cut -d'"' -f4)

# 3. Test POST /api/devices (Invalid IP format)
echo ""
echo "--- 3. Testing Input Validation ---"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/devices" \
  -H "Content-Type: application/json" \
  -d '{"name": "Bad IP Node", "ipAddress": "300.400.500.600"}')
assert_status "POST /api/devices (Invalid IP -> 400 Bad Request)" 400 "$HTTP_CODE"

# 4. Test GET /api/devices/{id}
if [ -n "$DEVICE_ID" ]; then
  echo ""
  echo "--- 4. Testing Single Device & Metric Endpoints ---"
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/devices/${DEVICE_ID}")
  assert_status "GET /api/devices/{id}" 200 "$HTTP_CODE"

  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/metrics/${DEVICE_ID}/latest")
  assert_status "GET /api/metrics/{id}/latest" 200 "$HTTP_CODE"

  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/metrics/${DEVICE_ID}/all")
  assert_status "GET /api/metrics/{id}/all" 200 "$HTTP_CODE"

  echo ""
  echo "--- 5. Testing Device Deletion ---"
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "${BASE_URL}/api/devices/${DEVICE_ID}")
  assert_status "DELETE /api/devices/{id}" 200 "$HTTP_CODE"
fi

# 6. Test GET /api/devices/{id} (Not Found)
echo ""
echo "--- 6. Testing 404 Error Response ---"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/devices/nonexistent6650a1f2e4b0")
assert_status "GET /api/devices/invalid -> 404 Not Found" 404 "$HTTP_CODE"

echo ""
echo "============================================================"
echo "Summary: Passed: ${PASSED}, Failed: ${FAILED}"
echo "============================================================"
