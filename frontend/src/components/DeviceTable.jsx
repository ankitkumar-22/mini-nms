import { Link } from "react-router-dom";
import StatusBadge from "./StatusBadge";
import { formatRelativeTime } from "../utils/time";

export default function DeviceTable({ devices, onDeleteRequest }) {
  if (devices.length === 0) {
    return (
      <div className="p-lg text-center font-body-md text-body-md text-on-surface-variant">
        No devices found.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto custom-scrollbar">
      <table className="w-full text-left border-collapse">
        <thead>
          <tr className="bg-surface-container-low/50">
            <th className="px-lg py-md font-label-sm text-label-sm text-on-surface-variant uppercase tracking-widest border-b border-outline-variant/10">
              Device Name
            </th>
            <th className="px-lg py-md font-label-sm text-label-sm text-on-surface-variant uppercase tracking-widest border-b border-outline-variant/10">
              IP Address
            </th>
            <th className="px-lg py-md font-label-sm text-label-sm text-on-surface-variant uppercase tracking-widest border-b border-outline-variant/10">
              Status
            </th>
            <th className="px-lg py-md font-label-sm text-label-sm text-on-surface-variant uppercase tracking-widest border-b border-outline-variant/10">
              Last Checked
            </th>
            <th className="px-lg py-md font-label-sm text-label-sm text-on-surface-variant uppercase tracking-widest border-b border-outline-variant/10 text-right">
              Actions
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-outline-variant/5">
          {devices.map((device) => (
            <tr key={device.id} className="hover:bg-surface-tint/[0.03] transition-colors group">
              <td className="px-lg py-md">
                <span className="font-body-lg text-on-surface font-semibold">{device.name}</span>
              </td>
              <td className="px-lg py-md">
                <span className="font-label-md text-label-md text-on-surface-variant bg-background/40 px-sm py-xs rounded">
                  {device.ipAddress}
                </span>
              </td>
              <td className="px-lg py-md">
                <StatusBadge status={device.status} />
              </td>
              <td className="px-lg py-md">
                <span className="font-label-md text-label-md text-on-surface-variant">
                  {formatRelativeTime(device.lastChecked)}
                </span>
              </td>
              <td className="px-lg py-md text-right">
                <Link
                  to={`/history?deviceId=${device.id}`}
                  className="inline-flex p-sm text-on-surface-variant hover:text-surface-tint transition-colors"
                  aria-label={`View history for ${device.name}`}
                >
                  <span className="material-symbols-outlined">history</span>
                </Link>
                <button
                  className="p-sm text-on-surface-variant hover:text-error transition-colors"
                  onClick={() => onDeleteRequest(device)}
                  aria-label={`Delete ${device.name}`}
                >
                  <span className="material-symbols-outlined">delete</span>
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
