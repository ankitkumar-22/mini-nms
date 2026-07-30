export default function SearchInput({ value, onChange, placeholder, className = "" }) {
  return (
    <div className={`relative group ${className}`}>
      <span className="material-symbols-outlined absolute left-md top-1/2 -translate-y-1/2 text-on-surface-variant text-[18px]">
        search
      </span>
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className="w-full bg-surface-container-lowest border border-outline-variant/20 rounded-full py-sm pl-xl pr-md text-body-md text-on-surface focus:outline-none focus:ring-2 focus:ring-surface-tint transition-all placeholder:text-on-surface-variant/40"
      />
    </div>
  );
}
