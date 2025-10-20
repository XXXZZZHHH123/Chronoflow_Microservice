#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${TASK_SERVICE_BASE_URL:-}" ]]; then
  echo "TASK_SERVICE_BASE_URL is required" >&2
  exit 1
fi

export MAVEN_OPTS="${MAVEN_OPTS:--Xms256m -Xmx512m -XX:+UseContainerSupport}"

declare -a args
args=(mvn -pl task-service -am io.gatling:gatling-maven-plugin:test -Dgatling.skip=false "-DtaskService.baseUrl=${TASK_SERVICE_BASE_URL}")

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
exec "${args[@]}"
