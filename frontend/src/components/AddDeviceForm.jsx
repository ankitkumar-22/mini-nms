import { useState } from "react";
import { createDevice } from "../api/devices";
import { extractApiError } from "../api/client";

const initialForm = { name: "", ipAddress: "" };

export default function AddDeviceForm({ onCreated }) {
  const [form, setForm] = useState(initialForm);
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  function updateField(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
    setFieldErrors((fe) => ({ ...fe, [field]: undefined }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setFieldErrors({});
    try {
      const device = await createDevice(form);
      setForm(initialForm);
      onCreated?.(device);
    } catch (err) {
      const { message, fieldErrors: apiFieldErrors } = extractApiError(err);
      if (Object.keys(apiFieldErrors).length > 0) {
        setFieldErrors(apiFieldErrors);
      } else if (err?.response?.status === 409) {
        setFieldErrors({ ipAddress: message });
      } else {
        setFieldErrors({ form: message });
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="glass-panel p-lg rounded-xl mb-xl border border-surface-tint/20">
      <h3 className="font-headline-md text-headline-md text-on-surface mb-md">Add New Device</h3>
      <form className="flex items-start gap-lg flex-wrap md:flex-nowrap" onSubmit={handleSubmit}>
        <div className="flex-1 min-w-[250px]">
          <label className="block text-label-sm text-on-surface-variant uppercase mb-xs">Device Name</label>
          <input
            className={`w-full bg-surface-container-low border rounded-lg px-md py-sm text-body-md text-on-surface outline-none transition-all ${
              fieldErrors.name
                ? "border-error/50 focus:border-error focus:ring-1 focus:ring-error"
                : "border-outline-variant/20 focus:ring-2 focus:ring-surface-tint"
            }`}
            placeholder="e.g. Core Router"
            type="text"
            value={form.name}
            onChange={(e) => updateField("name", e.target.value)}
          />
          {fieldErrors.name && (
            <p className="text-label-sm text-error mt-xs flex items-center gap-xs">
              <span className="material-symbols-outlined text-[14px]">error</span>
              {fieldErrors.name}
            </p>
          )}
        </div>
        <div className="flex-1 min-w-[250px]">
          <label className="block text-label-sm text-on-surface-variant uppercase mb-xs">IP Address (IPv4)</label>
          <input
            className={`w-full bg-surface-container-low border rounded-lg px-md py-sm text-body-md text-on-surface outline-none transition-all ${
              fieldErrors.ipAddress
                ? "border-error/50 focus:border-error focus:ring-1 focus:ring-error"
                : "border-outline-variant/20 focus:ring-2 focus:ring-surface-tint"
            }`}
            placeholder="192.168.1.1"
            type="text"
            value={form.ipAddress}
            onChange={(e) => updateField("ipAddress", e.target.value)}
          />
          {fieldErrors.ipAddress && (
            <p className="text-label-sm text-error mt-xs flex items-center gap-xs">
              <span className="material-symbols-outlined text-[14px]">error</span>
              {fieldErrors.ipAddress}
            </p>
          )}
        </div>
        <div className="pt-[22px]">
          <button
            className="px-lg py-sm rounded-lg bg-surface-tint text-on-primary font-bold shadow-[0_0_15px_rgba(0,218,243,0.3)] hover:shadow-[0_0_25px_rgba(0,218,243,0.5)] transition-all active:scale-95 h-[40px] flex items-center gap-sm disabled:opacity-50"
            type="submit"
            disabled={submitting}
          >
            <span className="material-symbols-outlined text-md">add</span>
            <span className="font-label-md text-label-md">{submitting ? "Adding…" : "Add Device"}</span>
          </button>
        </div>
      </form>
      {fieldErrors.form && (
        <p className="text-label-sm text-error mt-md flex items-center gap-xs">
          <span className="material-symbols-outlined text-[14px]">error</span>
          {fieldErrors.form}
        </p>
      )}
    </div>
  );
}
