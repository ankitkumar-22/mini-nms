export default function DeleteConfirmModal({ device, onConfirm, onCancel, deleting }) {
  if (!device) return null;

  return (
    <div className="fixed inset-0 z-[100] bg-background/80 backdrop-blur-sm flex items-center justify-center">
      <div className="glass-panel p-lg rounded-xl max-w-md w-full mx-md border border-error/30 shadow-[0_0_30px_rgba(255,180,171,0.1)]">
        <div className="flex items-center gap-md mb-md text-error">
          <span className="material-symbols-outlined text-[32px]">warning</span>
          <h3 className="font-headline-md text-headline-md text-on-surface">Confirm Deletion</h3>
        </div>
        <p className="text-body-md text-on-surface-variant mb-lg">
          Are you sure you want to delete <span className="font-bold text-on-surface">{device.name}</span>? This
          action cannot be undone and will stop monitoring for {device.ipAddress}.
        </p>
        <div className="flex justify-end gap-md">
          <button
            className="px-md py-sm rounded-lg border border-outline-variant/30 text-on-surface hover:bg-surface-container-highest transition-colors font-label-md disabled:opacity-50"
            onClick={onCancel}
            disabled={deleting}
          >
            Cancel
          </button>
          <button
            className="px-md py-sm rounded-lg bg-error text-on-error font-bold shadow-[0_0_15px_rgba(255,180,171,0.3)] hover:shadow-[0_0_25px_rgba(255,180,171,0.5)] transition-all font-label-md disabled:opacity-50"
            onClick={onConfirm}
            disabled={deleting}
          >
            {deleting ? "Deleting…" : "Delete Device"}
          </button>
        </div>
      </div>
    </div>
  );
}
