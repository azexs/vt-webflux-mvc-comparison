#!/bin/bash

# Lista endpointów
endpoints=("api/db")

declare -A port_to_app=(
  [8081]="web-mvc-vt-app"
)

# Lista VUs
vus=(5000)

PROMETHEUS_URL="http://localhost:9090/api/v1/write"

manage_container() {
  local action=$1
  local app_name=$2
  local container_name=$(docker ps -a --filter "name=$app_name" --format "{{.Names}}" | head -n 1)

  if [ -n "$container_name" ]; then
    case $action in
      start)
        echo "Starting container: $container_name"
        docker start "$container_name" > /dev/null 2>&1
        wait_for_container_ready "$container_name" "http://localhost:${port}/actuator/health"
        ;;
      stop)
        echo "Stopping container: $container_name"
        docker stop "$container_name" > /dev/null 2>&1
        ;;
      *)
        echo "Invalid action: $action"
        ;;
    esac
  else
    echo "No container found for app: $app_name"
  fi
}

wait_for_container_ready() {
  local container_name=$1
  local health_url=$2

  for i in {1..30}; do
    if curl -H "Accept: application/json" -s "$health_url" | grep -q '"status":"UP"'; then
      echo "Container $container_name is ready."
      return 0
    fi
    sleep 10
  done

  echo "Container $container_name did not become ready in time!"
  exit 1
}

for endpoint in "${endpoints[@]}"; do
  for port in "${!port_to_app[@]}"; do
    for vu in "${vus[@]}"; do

      app_name=${port_to_app[$port]}

      formatted_endpoint=${endpoint//\//-}

      folder="results/${formatted_endpoint}/${vu}"
      mkdir -p "$folder"

      manage_container start "$app_name"

      test_start_time=$(date '+%Y-%m-%d %H:%M:%S')

      echo "Starting test for endpoint: $endpoint, port: $port, VUs: $vu at $test_start_time"

      cat <<EOF > test_script.js
import http from 'k6/http';
import { check } from 'k6';

export let options = {
    scenarios: {
        warmup: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '15s', target: $vu },
            ],
            gracefulStop: '15s',
        },
        steady_load: {
            executor: 'constant-vus',
            vus: $vu,
            duration: '2m30s',
            startTime: '15s',
            gracefulStop: '15s',
        },
        rampdown: {
            executor: 'ramping-vus',
            startVUs: $vu,
            stages: [
                { duration: '15s', target: 0 },
            ],
            startTime: '2m45s',
            gracefulStop: '15s',
        },
    },
};

const endpoint = '$endpoint';

export default function () {
    const res = http.get(\`http://localhost:$port/\${endpoint}\`);
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
    return res;
}
EOF

      output_file="$folder/${app_name}-2.txt"

      K6_PROMETHEUS_RW_SERVER_URL=$PROMETHEUS_URL k6 run -o experimental-prometheus-rw test_script.js > "$output_file"

      test_end_time=$(date '+%Y-%m-%d %H:%M:%S')

      echo "Test completed for endpoint: $endpoint, port: $port, VUs: $vu at $test_end_time"

      echo -e "\nTest Start Time: $test_start_time" >> "$output_file"
      echo "Test End Time: $test_end_time" >> "$output_file"

      manage_container stop "$app_name"

      sleep 60
    done
  done
done
