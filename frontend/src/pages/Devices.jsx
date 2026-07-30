import { useEffect, useMemo, useState } from "react";
import SearchInput from "../components/SearchInput";
import AddDeviceForm from "../components/AddDeviceForm";
import DeviceTable from "../components/DeviceTable";
import DeleteConfirmModal from "../components/DeleteConfirmModal";
import ErrorBanner from "../components/ErrorBanner";
import { useDevices } from "../hooks/useDevices";
import { deleteDevice } from "../api/devices";

const PAGE_SIZE = 8;

export default function Devices() {
  const { devices, loading, error, refresh } = useDevices();
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(1);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return devices;
    return devices.filter(
      (d) => d.name.toLowerCase().includes(q) || d.ipAddress.toLowerCase().includes(q),
    );
  }, [devices, query]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));

  useEffect(() => setPage(1), [query]);
  useEffect(() => {
    if (page > totalPages) setPage(totalPages);
  }, [page, totalPages]);

  const pageItems = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);
  const rangeStart = filtered.length === 0 ? 0 : (page - 1) * PAGE_SIZE + 1;
  const rangeEnd = Math.min(page * PAGE_SIZE, filtered.length);

  async function handleDeleteConfirm() {
    if (!deleteTarget) return;
    setDeleting(true);
    setDeleteError(null);
    try {
      await deleteDevice(deleteTarget.id);
      setDeleteTarget(null);
      refresh();
    } catch {
      setDeleteError("Failed to delete device. Please try again.");
    } finally {
      setDeleting(false);
    }
  }

  return (
    <>
      <header className="sticky top-0 z-30 h-16 bg-background/80 backdrop-blur-md border-b border-outline-variant/10 flex items-center px-lg">
        <SearchInput value={query} onChange={setQuery} placeholder="Search network..." className="w-96" />
      </header>

      <main className="p-xl">
        <div className="max-w-container-max mx-auto">
          <div className="mb-xl">
            <h2 className="font-headline-lg text-headline-lg text-on-surface mb-xs">Manage Devices</h2>
            <nav className="flex text-label-sm text-on-surface-variant gap-xs">
              <span>Network</span>
              <span>/</span>
              <span className="text-surface-tint">Manage Devices</span>
            </nav>
          </div>

          {error && (
            <div className="mb-xl">
              <ErrorBanner message={error} />
            </div>
          )}
          {deleteError && (
            <div className="mb-xl">
              <ErrorBanner message={deleteError} />
            </div>
          )}

          <AddDeviceForm onCreated={refresh} />

          <div className="glass-panel rounded-xl overflow-hidden shadow-2xl">
            <div className="p-lg border-b border-outline-variant/10 flex justify-between items-center bg-surface-container/30">
              <h4 className="font-headline-md text-headline-md text-on-surface">Registered Inventory</h4>
            </div>

            {loading ? (
              <p className="p-lg font-body-md text-body-md text-on-surface-variant">Loading devices…</p>
            ) : (
              <DeviceTable devices={pageItems} onDeleteRequest={setDeleteTarget} />
            )}

            <div className="px-lg py-md bg-surface-container-low/30 border-t border-outline-variant/10 flex justify-between items-center">
              <p className="text-label-md text-on-surface-variant">
                Showing {rangeStart} to {rangeEnd} of {filtered.length} device{filtered.length === 1 ? "" : "s"}
              </p>
              <div className="flex gap-sm items-center">
                <button
                  className="p-sm rounded border border-outline-variant/20 hover:bg-surface-container-highest transition-colors disabled:opacity-30"
                  onClick={() => setPage((p) => Math.max(1, p - 1))}
                  disabled={page <= 1}
                >
                  <span className="material-symbols-outlined text-md">chevron_left</span>
                </button>
                <span className="font-label-md text-label-md text-on-surface-variant px-sm">
                  Page {page} of {totalPages}
                </span>
                <button
                  className="p-sm rounded border border-outline-variant/20 hover:bg-surface-container-highest transition-colors disabled:opacity-30"
                  onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                  disabled={page >= totalPages}
                >
                  <span className="material-symbols-outlined text-md">chevron_right</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>

      <DeleteConfirmModal
        device={deleteTarget}
        deleting={deleting}
        onCancel={() => setDeleteTarget(null)}
        onConfirm={handleDeleteConfirm}
      />
    </>
  );
}
