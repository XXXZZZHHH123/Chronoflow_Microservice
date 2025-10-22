#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${TASK_SERVICE_BASE_URL:-}" ]]; then
  echo "TASK_SERVICE_BASE_URL is required" >&2
  exit 1
fi

cd /workspace

export MAVEN_OPTS="${MAVEN_OPTS:--Xms256m -Xmx512m -XX:+UseContainerSupport}"

echo "PWD=$(pwd)"
echo "Listing modules at repo root:"
ls -la || true

echo "Preparing local Maven repository for task-service..."
mvn -B -U -pl task-service -am -DskipTests install

declare -a args
args=(
  mvn -B -pl task-service -am \
    io.gatling:gatling-maven-plugin:4.20.6:test \
    -Dgatling.skip=false \
    -Dgatling.failOnAssertionFailure=false \
    -DtaskService.baseUrl="${TASK_SERVICE_BASE_URL}"
)
      
if [[ -n "${TASK_SERVICE_LOGIN_PATH:-}" ]]; then
  args+=("-DtaskService.loginPath=${TASK_SERVICE_LOGIN_PATH}")
fi
if [[ -n "${TASK_SERVICE_USERNAME:-}" ]]; then
  args+=("-DtaskService.username=${TASK_SERVICE_USERNAME}")
fi
if [[ -n "${TASK_SERVICE_PASSWORD:-}" ]]; then
  args+=("-DtaskService.password=${TASK_SERVICE_PASSWORD}")
fi
if [[ -n "${TASK_SERVICE_EVENT_ID:-}" ]]; then
  args+=("-DtaskService.eventId=${TASK_SERVICE_EVENT_ID}")
fi
if [[ -n "${TASK_SERVICE_TASK_ID:-}" ]]; then
  args+=("-DtaskService.taskId=${TASK_SERVICE_TASK_ID}")
fi

echo "Starting Gatling with command: ${args[*]}"
set +e
"${args[@]}"
status=$?
set -e

RESULTS_DIR="/workspace/task-service/target/gatling"

if [[ -n "${TASK_SERVICE_RESULTS_UPLOAD_URL:-}" ]]; then
  if [[ -d "${RESULTS_DIR}" ]]; then
    ARCHIVE="/tmp/gatling-results.tgz"
    echo "Archiving Gatling results from ${RESULTS_DIR}..."
    tar -czf "${ARCHIVE}" -C "${RESULTS_DIR}" .
    echo "Uploading Gatling results archive to signed URL."
    if curl -sS --fail -X PUT -T "${ARCHIVE}" -H "Content-Type: application/gzip" "${TASK_SERVICE_RESULTS_UPLOAD_URL}"; then
      echo "Upload succeeded."
    else
      echo "Failed to upload Gatling results to signed URL." >&2
      status=1
    fi
    rm -f "${ARCHIVE}"
  else
    echo "Results directory ${RESULTS_DIR} not found; skipping upload."
  fi
fi

HOLD_SECONDS="${GATLING_HOLD_SECONDS:-300}"
if [ "$HOLD_SECONDS" -gt 0 ]; then
  echo "Gatling finished with status ${status}. Holding pod for ${HOLD_SECONDS}s to allow artifact collection..."
  sleep "${HOLD_SECONDS}"
fi

exit "$status"
