import { useEffect, useState } from "react";
import { fetchLatestMetrics } from "../api/metrics";

export function useLatestMetrics(deviceId, enabled) {
  const [metrics, setMetrics] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!enabled || !deviceId) return;
    let cancelled = false;
    setLoading(true);
    fetchLatestMetrics(deviceId)
      .then((data) => {
        if (!cancelled) setMetrics(data);
      })
      .catch(() => {
        if (!cancelled) setMetrics([]);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [deviceId, enabled]);

  return { metrics, loading };
}
