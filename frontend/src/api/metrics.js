import { apiClient } from "./client";
import { toLocalDateTimeString } from "../utils/time";

export async function fetchLatestMetrics(deviceId) {
  const { data } = await apiClient.get(`/metrics/${deviceId}/latest`);
  return data;
}

export async function fetchAllMetrics(deviceId) {
  const { data } = await apiClient.get(`/metrics/${deviceId}/all`);
  return data;
}

export async function fetchMetricsRange(deviceId, start, end) {
  const { data } = await apiClient.get(`/metrics/${deviceId}/range`, {
    params: { start: toLocalDateTimeString(start), end: toLocalDateTimeString(end) },
  });
  return data;
}
