import { useMemo, useState } from "react";
import SearchInput from "../components/SearchInput";
import StatPill from "../components/StatPill";
import DeviceCard from "../components/DeviceCard";
import ErrorBanner from "../components/ErrorBanner";
import { useDevices } from "../hooks/useDevices";

export default function Dashboard() {
  const { devices, loading, error } = useDevices();
  const [query, setQuery] = useState("");

  const counts = useMemo(() => {
    const up = devices.filter((d) => d.status === "UP").length;
    const down = devices.filter((d) => d.status === "DOWN").length;
    return { total: devices.length, up, down };
  }, [devices]);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return devices;
    return devices.filter(
      (d) => d.name.toLowerCase().includes(q) || d.ipAddress.toLowerCase().includes(q),
    );
  }, [devices, query]);

  return (
    <>
      <header className="sticky top-0 z-30 h-16 bg-background/80 backdrop-blur-md border-b border-outline-variant/10 flex items-center px-lg gap-lg">
        <SearchInput
          value={query}
          onChange={setQuery}
          placeholder="Search devices..."
          className="w-96"
        />
        <div className="hidden md:flex items-center gap-lg ml-lg border-l border-outline-variant/20 pl-lg">
          <StatPill label="TOTAL DEVICES" value={counts.total} />
          <StatPill label="UP" value={counts.up} dotClassName="bg-surface-tint pulse-up" />
          <StatPill label="DOWN" value={counts.down} dotClassName="bg-error" valueClassName="text-error" />
        </div>
      </header>

      <main className="p-xl">
        <div className="max-w-container-max mx-auto space-y-lg">
          <div className="flex justify-between items-center mb-md">
            <h2 className="font-headline-lg text-headline-lg text-on-surface">Monitored Devices</h2>
          </div>

          {error && <ErrorBanner message={error} />}

          {!error && loading && (
            <p className="font-body-md text-body-md text-on-surface-variant">Loading devices…</p>
          )}

          {!error && !loading && filtered.length === 0 && (
            <p className="font-body-md text-body-md text-on-surface-variant">
              {devices.length === 0
                ? "No devices registered yet. Add one from the Devices page."
                : "No devices match your search."}
            </p>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-lg">
            {filtered.map((device) => (
              <DeviceCard key={device.id} device={device} />
            ))}
          </div>
        </div>
      </main>
    </>
  );
}
