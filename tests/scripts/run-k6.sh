#!/usr/bin/env bash
set -euo pipefail

BASE_URL=${BASE_URL:-http://localhost:8080}
REPORT_DIR="tests/reports"
mkdir -p "${REPORT_DIR}"

timestamp() {
  date +"%Y%m%d_%H%M%S"
}

preflight() {
  echo "Checking ${BASE_URL}..."
  if ! curl -sf "${BASE_URL}/v3/api-docs" >/dev/null; then
    echo "Base URL not reachable. Start the app before running k6."
    exit 1
  fi
}

run_test() {
  local name="$1"
  local script="$2"
  local now
  now=$(timestamp)
  echo "Running ${name}..."
  BASE_URL="${BASE_URL}" k6 run "${script}"
  if [ -f "${REPORT_DIR}/${name}_summary.json" ]; then
    mv "${REPORT_DIR}/${name}_summary.json" "${REPORT_DIR}/${name}_${now}.json"
  fi
}

preflight

run_test "smoke" "tests/k6/smoke.js"
run_test "load" "tests/k6/load.js"
run_test "spike" "tests/k6/spike.js"
run_test "idempotency" "tests/k6/idempotency.js"

echo "Reports saved in ${REPORT_DIR}/"
