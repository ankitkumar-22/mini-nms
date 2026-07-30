import { useCallback, useEffect, useRef, useState } from "react";
import { fetchDevices } from "../api/devices";

const POLL_INTERVAL_MS = 10000;

export function useDevices() {
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const pollRef = useRef(null);

  const load = useCallback(async ({ silent } = {}) => {
    if (!silent) setLoading(true);
    try {
      const data = await fetchDevices();
      setDevices(data);
      setError(null);
    } catch (err) {
      setError("Unable to reach the Mini-NMS API. Is the backend running on :8080?");
    } finally {
      if (!silent) setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
    pollRef.current = setInterval(() => load({ silent: true }), POLL_INTERVAL_MS);
    return () => clearInterval(pollRef.current);
  }, [load]);

  return { devices, loading, error, refresh: () => load({ silent: true }) };
}
