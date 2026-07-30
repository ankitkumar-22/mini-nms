import { useEffect, useState } from "react";
import { fetchAllMetrics, fetchMetricsRange } from "../api/metrics";

const RANGE_HOURS = { "1h": 1, "6h": 6, "24h": 24, "7d": 24 * 7 };

function rangeStartFor(presetKey) {
  const hours = RANGE_HOURS[presetKey] ?? 24;
  return new Date(Date.now() - hours * 60 * 60 * 1000);
}

export function useHistoryMetrics(deviceId, presetKey) {
  const [metrics, setMetrics] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!deviceId) {
      setMetrics([]);
      return;
    }
    let cancelled = false;
    setLoading(true);
    setError(null);

    const request =
      presetKey === "all"
        ? fetchAllMetrics(deviceId)
        : fetchMetricsRange(deviceId, rangeStartFor(presetKey), new Date());

    request
      .then((data) => {
        if (!cancelled) setMetrics(data);
      })
      .catch(() => {
        if (!cancelled) setError("Unable to load history for this device.");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [deviceId, presetKey]);

  return { metrics, loading, error };
}
