import { useEffect, useMemo } from "react";
import { useSearchParams } from "react-router-dom";
import { useDevices } from "../hooks/useDevices";
import { useHistoryMetrics } from "../hooks/useHistoryMetrics";
import StatusTimeline from "../components/StatusTimeline";
import TimeSeriesChart from "../components/TimeSeriesChart";
import ErrorBanner from "../components/ErrorBanner";

const RANGE_OPTIONS = [
  { key: "1h", label: "1H" },
  { key: "6h", label: "6H" },
  { key: "24h", label: "24H" },
  { key: "7d", label: "7D" },
  { key: "all", label: "ALL" },
];

export default function History() {
  const { devices, loading: devicesLoading, error: devicesError } = useDevices();
  const [searchParams, setSearchParams] = useSearchParams();

  const deviceId = searchParams.get("deviceId") || "";
  const range = searchParams.get("range") || "24h";

  useEffect(() => {
    if (!deviceId && devices.length > 0) {
      setSearchParams({ deviceId: devices[0].id, range }, { replace: true });
    }
  }, [deviceId, devices, range, setSearchParams]);

  const selectedDevice = devices.find((d) => d.id === deviceId);
  const { metrics, loading: metricsLoading, error: metricsError } = useHistoryMetrics(deviceId, range);

  const stats = useMemo(() => {
    if (metrics.length === 0) return null;
    const upCount = metrics.filter((m) => m.status === "UP").length;
    // latencyMs is -1 when a ping fails (device unreachable) - not a real latency sample.
    const latencies = metrics.map((m) => m.latencyMs).filter((v) => v >= 0);
    const packetLosses = metrics.map((m) => m.packetLoss);
    return {
      total: metrics.length,
      uptimePct: (upCount / metrics.length) * 100,
      avgLatency: latencies.length ? latencies.reduce((a, b) => a + b, 0) / latencies.length : null,
      maxLatency: latencies.length ? Math.max(...latencies) : null,
      avgPacketLoss: packetLosses.reduce((a, b) => a + b, 0) / packetLosses.length,
    };
  }, [metrics]);

  function selectDevice(id) {
    setSearchParams({ deviceId: id, range });
  }

  function selectRange(key) {
    setSearchParams({ deviceId, range: key });
  }

  return (
    <>
      <header className="sticky top-0 z-30 h-16 bg-background/80 backdrop-blur-md border-b border-outline-variant/10 flex items-center px-lg gap-lg">
        <select
          className="bg-surface-container-lowest border border-outline-variant/20 rounded-lg px-md py-sm text-body-md text-on-surface focus:outline-none focus:ring-2 focus:ring-surface-tint min-w-[220px]"
          value={deviceId}
          onChange={(e) => selectDevice(e.target.value)}
        >
          {devices.length === 0 && <option value="">No devices registered</option>}
          {devices.map((d) => (
            <option key={d.id} value={d.id}>
              {d.name} ({d.ipAddress})
            </option>
          ))}
        </select>

        <div className="flex gap-xs">
          {RANGE_OPTIONS.map((opt) => (
            <button
              key={opt.key}
              onClick={() => selectRange(opt.key)}
              className={`px-md py-xs rounded font-label-md text-label-md transition-colors ${
                range === opt.key
                  ? "bg-surface-tint text-on-primary font-bold"
                  : "text-on-surface-variant hover:bg-surface-container-highest"
              }`}
            >
              {opt.label}
            </button>
          ))}
        </div>
      </header>

      <main className="p-xl">
        <div className="max-w-container-max mx-auto space-y-lg">
          <h2 className="font-headline-lg text-headline-lg text-on-surface">Device History</h2>

          {devicesError && <ErrorBanner message={devicesError} />}
          {metricsError && <ErrorBanner message={metricsError} />}

          {!devicesLoading && devices.length === 0 && (
            <p className="font-body-md text-body-md text-on-surface-variant">
              No devices registered yet. Add one from the Devices page.
            </p>
          )}

          {selectedDevice && (
            <>
              <div className="glass-panel rounded-xl p-lg">
                <div className="flex justify-between items-start mb-md">
                  <div>
                    <h3 className="font-headline-md text-headline-md text-on-surface">{selectedDevice.name}</h3>
                    <p className="font-label-sm text-label-sm text-on-surface-variant mt-xs">
                      {selectedDevice.ipAddress}
                    </p>
                  </div>
                </div>

                {metricsLoading ? (
                  <p className="font-body-md text-body-md text-on-surface-variant">Loading history…</p>
                ) : metrics.length === 0 ? (
                  <p className="font-body-md text-body-md text-on-surface-variant">
                    No metrics recorded for this device in the selected range.
                  </p>
                ) : (
                  <>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-md mb-lg">
                      <StatCard label="Uptime" value={`${stats.uptimePct.toFixed(1)}%`} />
                      <StatCard
                        label="Avg Latency"
                        value={stats.avgLatency !== null ? `${stats.avgLatency.toFixed(0)}ms` : "—"}
                      />
                      <StatCard
                        label="Max Latency"
                        value={stats.maxLatency !== null ? `${stats.maxLatency.toFixed(0)}ms` : "—"}
                      />
                      <StatCard label="Avg Packet Loss" value={`${stats.avgPacketLoss.toFixed(1)}%`} />
                    </div>

                    <div className="mb-lg">
                      <span className="font-label-sm text-label-sm text-on-surface-variant uppercase block mb-xs">
                        Status ({stats.total} checks)
                      </span>
                      <StatusTimeline
                        points={metrics.map((m) => ({ timestamp: m.timestamp, status: m.status }))}
                      />
                    </div>

                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-lg">
                      <div>
                        <span className="font-label-sm text-label-sm text-on-surface-variant uppercase block mb-xs">
                          Latency
                        </span>
                        <TimeSeriesChart
                          points={metrics
                            .filter((m) => m.latencyMs >= 0)
                            .map((m) => ({ timestamp: m.timestamp, value: m.latencyMs }))}
                          color="#00daf3"
                          formatValue={(v) => `${v.toFixed(0)}ms`}
                        />
                      </div>
                      <div>
                        <span className="font-label-sm text-label-sm text-on-surface-variant uppercase block mb-xs">
                          Packet Loss
                        </span>
                        <TimeSeriesChart
                          points={metrics.map((m) => ({ timestamp: m.timestamp, value: m.packetLoss }))}
                          color="#ffb4ab"
                          formatValue={(v) => `${v.toFixed(1)}%`}
                        />
                      </div>
                    </div>
                  </>
                )}
              </div>
            </>
          )}
        </div>
      </main>
    </>
  );
}

function StatCard({ label, value }) {
  return (
    <div className="bg-surface-container-low/50 rounded-lg p-md border border-outline-variant/10">
      <p className="font-label-sm text-label-sm text-on-surface-variant uppercase mb-xs">{label}</p>
      <p className="font-headline-md text-headline-md text-on-surface">{value}</p>
    </div>
  );
}
