const COLOR_BY_STATUS = {
  UP: "#00daf3",
  DOWN: "#ffb4ab",
  UNKNOWN: "#849396",
};

export default function StatusTimeline({ points, height = 40 }) {
  if (!points || points.length === 0) {
    return (
      <div
        className="w-full flex items-center justify-center text-label-sm text-on-surface-variant/50 bg-surface-container-lowest rounded"
        style={{ height }}
      >
        No status history in this range
      </div>
    );
  }

  const times = points.map((p) => new Date(p.timestamp).getTime());
  const minT = Math.min(...times);
  const maxT = Math.max(...times);
  const segmentWidth = Math.min(2.5, Math.max(0.4, 100 / points.length));

  return (
    <svg
      className="w-full rounded"
      style={{ height, backgroundColor: "#0d1c2d" }}
      viewBox="0 0 100 10"
      preserveAspectRatio="none"
    >
      {points.map((p, i) => {
        const t = times[i];
        const x = maxT > minT ? ((t - minT) / (maxT - minT)) * 100 : 50;
        return (
          <rect
            key={i}
            x={Math.max(0, x - segmentWidth / 2)}
            y={0}
            width={segmentWidth}
            height={10}
            fill={COLOR_BY_STATUS[p.status] ?? COLOR_BY_STATUS.UNKNOWN}
          />
        );
      })}
    </svg>
  );
}
