export default function ErrorBanner({ message }) {
  if (!message) return null;
  return (
    <div className="glass-panel border border-error/30 rounded-xl px-lg py-md flex items-center gap-md text-error">
      <span className="material-symbols-outlined">error</span>
      <span className="font-body-md text-body-md">{message}</span>
    </div>
  );
}
