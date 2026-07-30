import { NavLink } from "react-router-dom";

const linkBase =
  "flex items-center gap-md px-md py-sm rounded-lg font-body-md text-body-md transition-colors duration-200 active:scale-95";

function navLinkClass({ isActive }) {
  return isActive
    ? `${linkBase} bg-secondary-container text-on-secondary-container font-bold`
    : `${linkBase} text-on-surface-variant hover:text-on-surface hover:bg-surface-container-highest`;
}

export default function Sidebar() {
  return (
    <aside className="h-screen w-64 fixed left-0 top-0 bg-surface-container-lowest border-r border-outline-variant/10 flex flex-col p-md z-40">
      <div className="mb-xl px-sm">
        <h1 className="font-headline-md text-headline-md font-bold text-surface-tint">Mini-NMS</h1>
        <p className="font-label-md text-label-md text-on-surface-variant opacity-70">Vigilant Monitor</p>
      </div>

      <nav className="flex-1 space-y-base">
        <NavLink to="/" end className={navLinkClass}>
          <span className="material-symbols-outlined">dashboard</span>
          <span>Dashboard</span>
        </NavLink>
        <NavLink to="/devices" className={navLinkClass}>
          <span className="material-symbols-outlined">router</span>
          <span>Devices</span>
        </NavLink>
        <NavLink to="/history" className={navLinkClass}>
          <span className="material-symbols-outlined">history</span>
          <span>History</span>
        </NavLink>
      </nav>

      <div className="mt-auto border-t border-outline-variant/10 pt-md">
        <NavLink
          to="/devices"
          className="w-full bg-surface-tint text-background font-bold py-sm rounded-lg flex items-center justify-center gap-sm transition-transform active:scale-95"
        >
          <span className="material-symbols-outlined">add</span>
          <span className="font-label-md text-label-md">Add Device</span>
        </NavLink>
      </div>
    </aside>
  );
}
