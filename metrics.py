import requests
import json
import os
from datetime import datetime


def query_prometheus(query, time):
  prometheus_url = "http://localhost:9090/api/v1/query"
  try:
    response = requests.get(prometheus_url, params={
      "query": query,
      "time": time
    })
    response.raise_for_status()
    data = response.json()
    if "data" in data and "result" in data["data"]:
      return float(data["data"]["result"][0]["value"][1])  # Wartość metryki
  except Exception as e:
    print(f"Error querying Prometheus (query) for query: {query}, Error: {e}")
  return None


def fetch_metrics(start_time, end_time, container_name, endpoint, vus_suffix, app, output_dir):
  range_duration = f"{end_time - start_time}s"

  queries = {
    "cpu_avg": f"avg_over_time(irate(container_cpu_usage_seconds_total{{name='{container_name}'}}[1m])[{range_duration}:]) * 100 / 12",
    "cpu_max": f"max_over_time(irate(container_cpu_usage_seconds_total{{name='{container_name}'}}[1m])[{range_duration}:]) * 100 / 12",
    "memory_avg": f"avg_over_time(container_memory_working_set_bytes{{name='{container_name}'}}[{range_duration}])",
    "memory_max": f"max_over_time(container_memory_working_set_bytes{{name='{container_name}'}}[{range_duration}])",

    "threads_max": f"max_over_time(jvm_threads_live_threads{{application='{app}'}}[{range_duration}])",
    "jvm_memory_avg": f"sum(avg_over_time(jvm_memory_used_bytes{{application='{app}'}}[{range_duration}]))",
    "jvm_memory_max": f"sum(max_over_time(jvm_memory_used_bytes{{application='{app}'}}[{range_duration}]))",
    "jvm_system_cpu_avg": f"avg_over_time(system_cpu_usage{{application='{app}'}}[{range_duration}]) * 100 ",
    "jvm_system_cpu_max": f"max_over_time(system_cpu_usage{{application='{app}'}}[{range_duration}]) * 100 ",
    "jvm_process_cpu_avg": f"avg_over_time(process_cpu_usage{{application='{app}'}}[{range_duration}]) * 100 ",
    "jvm_process_cpu_max": f"max_over_time(process_cpu_usage{{application='{app}'}}[{range_duration}]) * 100 ",
    "jvm_load_avg": f"avg_over_time(system_load_average_1m{{application='{app}'}}[{range_duration}])",
    "jvm_load_max": f"max_over_time(system_load_average_1m{{application='{app}'}}[{range_duration}])",
  }

  metrics_data = {}
  for metric_name, query in queries.items():
    point_data = query_prometheus(query, end_time)

    if "memory" in metric_name and point_data is not None:
      point_data = point_data / (1024 * 1024)

    metrics_data[metric_name] = round(point_data, 2) if point_data is not None else None

  output_file = os.path.join(output_dir, f"metrics_{app}.json")
  with open(output_file, "w") as json_file:
    json.dump(metrics_data, json_file, indent=4)

  print(f"Metrics saved to {output_file}")


def process_results():
  containers = [
    {"name": "vt-webflux-mvc-comparison-web-mvc-app-1", "port": 8080, "app": "web-mvc-app"},
    {"name": "vt-webflux-mvc-comparison-web-mvc-vt-app-1", "port": 8081, "app": "web-mvc-vt-app"},
    {"name": "vt-webflux-mvc-comparison-webflux-app-1", "port": 8082, "app": "webflux-app"},
  ]
  endpoints = ["api-db", "api-delay", "api-file", "api-hello"]
  vus_suffixes = ["1000", "5000"]

  for endpoint in endpoints:
    for vus_suffix in vus_suffixes:
      for container in containers:
        try:
          file_path = f"results/{endpoint}/{vus_suffix}/{container['app']}.txt"

          if not os.path.exists(file_path):
            print(f"File not found: {file_path}")
            continue

          with open(file_path, "r") as f:
            content = f.read()
            start_time = content.split("Test Start Time: ")[1].split("\n")[0].strip()
            end_time = content.split("Test End Time: ")[1].split("\n")[0].strip()

          start_timestamp = int(datetime.strptime(start_time, "%Y-%m-%d %H:%M:%S").timestamp())
          end_timestamp = int(datetime.strptime(end_time, "%Y-%m-%d %H:%M:%S").timestamp())

          output_dir = os.path.dirname(file_path)

          fetch_metrics(start_timestamp, end_timestamp, container["name"], endpoint, vus_suffix, container["app"], output_dir)
        except Exception as e:
          print(f"Error processing file {file_path}: {e}")

if __name__ == "__main__":
  process_results()
