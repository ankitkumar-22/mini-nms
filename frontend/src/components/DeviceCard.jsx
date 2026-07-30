import { useState } from "react";
import { Link } from "react-router-dom";
import StatusBadge from "./StatusBadge";
import Sparkline from "./Sparkline";
import { useLatestMetrics } from "../hooks/useLatestMetrics";
import { formatRelativeTime } from "../utils/time";

const BORDER_BY_STATUS = {
  UP: "border-surface-tint/30",
  DOWN: "border-error/30",
  UNKNOWN: "border-outline-variant/20",
};

const COLOR_BY_STATUS = {
  UP: "#00daf3",
  DOWN: "#ffb4ab",
  UNKNOWN: "#849396",
};

export default function DeviceCard({ device }) {
  const [expanded, setExpanded] = useState(false);
  const { metrics, loading } = useLatestMetrics(device.id, expanded);

  const chronological = [...metrics].reverse();
  const latencyValues = chronological.map((m) => m.latencyMs);
  const packetLossValues = chronological.map((m) => m.packetLoss);
  const avgLatency = latencyValues.length
    ? (latencyValues.reduce((a, b) => a + b, 0) / latencyValues.length).toFixed(0)
    : null;
  const latestPacketLoss = packetLossValues.length ? packetLossValues.at(-1) : null;
  const color = COLOR_BY_STATUS[device.status] ?? COLOR_BY_STATUS.UNKNOWN;

  return (
    <div
      className={`glass-panel rounded-xl p-lg flex flex-col gap-md transition-all cursor-pointer hover:border-surface-tint/30 border ${BORDER_BY_STATUS[device.status] ?? BORDER_BY_STATUS.UNKNOWN}`}
      onClick={() => setExpanded((e) => !e)}
    >
      <div className="flex justify-between items-start gap-md">
        <div className="min-w-0">
          <h3 className="font-headline-md text-headline-md text-on-surface truncate">{device.name}</h3>
          <p className="font-label-sm text-label-sm text-on-surface-variant mt-xs">{device.ipAddress}</p>
        </div>
        <div className="flex items-center gap-sm shrink-0">
          <StatusBadge status={device.status} />
          <Link
            to={`/history?deviceId=${device.id}`}
            onClick={(e) => e.stopPropagation()}
            className="text-on-surface-variant hover:text-surface-tint transition-colors"
            aria-label={`View history for ${device.name}`}
          >
            <span className="material-symbols-outlined text-[20px]">history</span>
          </Link>
        </div>
      </div>

      {expanded && (
        <div className="grid grid-cols-2 gap-md py-md border-y border-outline-variant/10">
          {loading ? (
            <p className="col-span-2 font-label-sm text-label-sm text-on-surface-variant">Loading metrics…</p>
          ) : metrics.length === 0 ? (
            <p className="col-span-2 font-label-sm text-label-sm text-on-surface-variant">
              No metrics recorded yet
            </p>
          ) : (
            <>
              <div className="flex flex-col gap-xs">
                <span className="font-label-sm text-label-sm text-on-surface-variant">Latency (recent)</span>
                <Sparkline values={latencyValues} color={color} />
                <span className="font-label-md text-label-md mt-xs" style={{ color }}>
                  {avgLatency !== null ? `${avgLatency}ms avg` : "—"}
                </span>
              </div>
              <div className="flex flex-col gap-xs">
                <span className="font-label-sm text-label-sm text-on-surface-variant">Packet Loss (recent)</span>
                <Sparkline values={packetLossValues} color={color} />
                <span className="font-label-md text-label-md mt-xs" style={{ color }}>
                  {latestPacketLoss !== null ? `${latestPacketLoss.toFixed(2)}%` : "—"}
                </span>
              </div>
            </>
          )}
        </div>
      )}

      <div className="flex justify-between items-center pt-sm border-t border-outline-variant/10 mt-auto">
        <span className="font-label-sm text-label-sm text-on-surface-variant">Last Checked</span>
        <span className="font-body-md text-body-md text-on-surface">
          {formatRelativeTime(device.lastChecked)}
        </span>
      </div>
    </div>
  );
}
