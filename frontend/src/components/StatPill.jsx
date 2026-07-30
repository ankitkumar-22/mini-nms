export default function StatPill({ label, value, dotClassName, valueClassName = "text-on-surface" }) {
  return (
    <div className="flex items-baseline gap-sm">
      {dotClassName && <span className={`h-2 w-2 rounded-full ${dotClassName}`} />}
      <span className="font-label-md text-label-md text-on-surface-variant">{label}</span>
      <span className={`font-headline-md text-headline-md ${valueClassName}`}>{value}</span>
    </div>
  );
}
