const STYLES = {
  UP: {
    wrap: "bg-surface-tint/10 text-surface-tint border-surface-tint/20",
    dot: "bg-surface-tint pulse-up",
  },
  DOWN: {
    wrap: "bg-error/10 text-error border-error/20",
    dot: "bg-error",
  },
  UNKNOWN: {
    wrap: "bg-outline-variant/10 text-on-surface-variant border-outline-variant/30",
    dot: "bg-outline",
  },
};

export default function StatusBadge({ status }) {
  const style = STYLES[status] ?? STYLES.UNKNOWN;
  return (
    <span
      className={`px-sm py-xs font-label-sm rounded border flex items-center gap-xs shrink-0 ${style.wrap}`}
    >
      <span className={`h-1.5 w-1.5 rounded-full ${style.dot}`} />
      {status ?? "UNKNOWN"}
    </span>
  );
}
