export default function TimeSeriesChart({ points, color = "#00daf3", height = 160, formatValue = (v) => v }) {
  if (!points || points.length < 2) {
    return (
      <div
        className="w-full flex items-center justify-center text-label-sm text-on-surface-variant/50"
        style={{ height }}
      >
        Not enough data in this range
      </div>
    );
  }

  const times = points.map((p) => new Date(p.timestamp).getTime());
  const values = points.map((p) => p.value);
  const minT = Math.min(...times);
  const maxT = Math.max(...times);
  const minV = Math.min(...values);
  const maxV = Math.max(...values);
  const vRange = maxV - minV || 1;
  const viewHeight = 100;

  const coords = points.map((p, i) => {
    const t = times[i];
    const x = maxT > minT ? ((t - minT) / (maxT - minT)) * 100 : 50;
    const y = viewHeight - ((p.value - minV) / vRange) * viewHeight;
    return [x, y];
  });

  const linePath = coords.map(([x, y], i) => `${i === 0 ? "M" : "L"}${x},${y}`).join(" ");
  const areaPath = `${linePath} L100,${viewHeight} L0,${viewHeight} Z`;

  const avg = values.reduce((a, b) => a + b, 0) / values.length;

  return (
    <div>
      <svg className="w-full" style={{ height }} viewBox={`0 0 100 ${viewHeight}`} preserveAspectRatio="none">
        <path d={areaPath} fill={color} fillOpacity="0.12" />
        <path d={linePath} fill="none" stroke={color} strokeWidth="1.5" vectorEffect="non-scaling-stroke" />
      </svg>
      <div className="flex justify-between font-label-sm text-label-sm text-on-surface-variant mt-xs">
        <span>Min {formatValue(minV)}</span>
        <span style={{ color }}>Avg {formatValue(avg)}</span>
        <span>Max {formatValue(maxV)}</span>
      </div>
    </div>
  );
}
