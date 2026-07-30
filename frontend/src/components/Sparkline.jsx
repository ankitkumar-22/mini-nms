export default function Sparkline({ values, color = "#00daf3", height = 48 }) {
  if (!values || values.length < 2) {
    return (
      <div
        className="w-full flex items-center text-label-sm text-on-surface-variant/50"
        style={{ height }}
      >
        Not enough data yet
      </div>
    );
  }

  const width = 100;
  const viewHeight = 30;
  const min = Math.min(...values);
  const max = Math.max(...values);
  const range = max - min || 1;
  const stepX = width / (values.length - 1);

  const points = values.map((v, i) => [i * stepX, viewHeight - ((v - min) / range) * viewHeight]);
  const linePath = points.map(([x, y], i) => `${i === 0 ? "M" : "L"}${x},${y}`).join(" ");
  const areaPath = `${linePath} L${width},${viewHeight} L0,${viewHeight} Z`;

  return (
    <svg className="w-full" style={{ height }} viewBox={`0 0 ${width} ${viewHeight}`} preserveAspectRatio="none">
      <path d={areaPath} fill={color} fillOpacity="0.12" />
      <path d={linePath} fill="none" stroke={color} strokeWidth="2" />
    </svg>
  );
}
